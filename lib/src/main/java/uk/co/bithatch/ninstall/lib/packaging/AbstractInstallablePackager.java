/*
 * Copyright © 2020 Bithatch (brett@bithatch.co.uk)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the “Software”), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package uk.co.bithatch.ninstall.lib.packaging;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import uk.co.bithatch.ninstall.lib.AttributeKey;
import uk.co.bithatch.ninstall.lib.Executable;
import uk.co.bithatch.ninstall.lib.IO;
import uk.co.bithatch.ninstall.lib.InputFileset;
import uk.co.bithatch.ninstall.lib.JarPath;
import uk.co.bithatch.ninstall.lib.Locations;
import uk.co.bithatch.ninstall.lib.OutputFileset;
import uk.co.bithatch.ninstall.lib.SetupAppFactory;

public abstract class AbstractInstallablePackager extends Packager {

    @SuppressWarnings("unchecked")
    public static abstract class Builder<PKGR extends AbstractInstallablePackager, BLDR extends Builder<PKGR, BLDR>>
            extends Packager.Builder<PKGR, Builder<PKGR, BLDR>> {
        private Optional<SetupAppFactory<?>> installer = Optional.empty();
        private Optional<SetupAppFactory<?>> uninstaller = Optional.empty();
        private Optional<SetupAppFactory<?>> updater = Optional.empty();
        private Optional<String> installerName = Optional.empty();
        private Optional<Integer> parallelism = Optional.empty();
        private final Map<String, Class<?>> otherApplications = new LinkedHashMap<>();;

        public BLDR withOther(String name, Class<?> app) {
        	otherApplications.put(name, app);
        	return (BLDR)this;
        }
        
        public BLDR withOthers(Map<String, Class<?>> others) {
        	otherApplications.putAll(others);
        	return (BLDR)this;
        }
        
        public BLDR withParallelism(int parallelism) {
        	this.parallelism = Optional.of(parallelism);
            return (BLDR) this;
        }

        public BLDR withInstaller(SetupAppFactory<?> installer) {
            Packager.checkExecutableTask(installer.getClass());
            this.installer = Optional.of(installer);
            return (BLDR) this;
        }
        
        public BLDR withUpdater(SetupAppFactory<?> updater) {
            Packager.checkExecutableTask(updater.getClass());
            this.updater = Optional.of(updater);
            return (BLDR) this;
        }

        public BLDR withoutInstallerName() {
            this.installerName = Optional.empty(); 
            return (BLDR) this;
        }
        
        public BLDR withInstallerName(String installerName) {
            this.installerName = Optional.of(installerName);
            return (BLDR) this;
        }

        public BLDR withUninstaller(SetupAppFactory<?> uninstaller) {
            Packager.checkExecutableTask(uninstaller.getClass());
            this.uninstaller = Optional.of(uninstaller);
            return (BLDR) this;
        }
    }

    private final Optional<SetupAppFactory<?>> installer;
    private final Optional<SetupAppFactory<?>> uninstaller;
    private final Optional<SetupAppFactory<?>> updater;
    private final Optional<String> installerName;
    private final Map<String, Class<?>> otherApplications;
    
    protected final Optional<Integer> parallelism;

    protected AbstractInstallablePackager(Builder<?, ?> bldr) {
        super(bldr);
        this.otherApplications =  Collections.unmodifiableMap(new LinkedHashMap<>(bldr.otherApplications));
        this.installer = bldr.installer;
        this.uninstaller = bldr.uninstaller;
        this.updater = bldr.updater;
        this.installerName = bldr.installerName;
        this.parallelism = bldr.parallelism;
    }
    
    public final Optional<SetupAppFactory<?>> installer() {
        return installer;
    }
    
    public final Optional<SetupAppFactory<?>> uninstaller() {
        return uninstaller;
    }
    
    public final Optional<SetupAppFactory<?>> updater() {
        return updater;
    }

    @Override
    protected int visit(PackagerContext ctx, PackageFileVisitor visitor) {
        var visited = new AtomicInteger(super.visit(ctx, visitor));
        var noInputs = visited.get() == 0;
        installer.ifPresent(i -> {
            buildExe(ctx, visitor, i, PackagerAttributes.INSTALLER_PATH, installerName);
            visited.incrementAndGet();
        });
        uninstaller.ifPresent(i -> {
            buildExe(ctx, visitor, i, null, Optional.of("uninstall"));
            visited.incrementAndGet();
        });
        updater.ifPresent(i -> {
            buildExe(ctx, visitor, i, null, Optional.of("update"));
            visited.incrementAndGet();
        });
        otherApplications.forEach((app, clazz) -> {
            buildOtherExe(ctx, visitor, clazz, null, Optional.of(app));
            visited.incrementAndGet();
        });

        if(noInputs) {
        	if(visited.get() == 0)
        		throw new IllegalStateException("No input files for package.");
        	else
        		ctx.progress().info("Only setup applications included, no additional resources");
        }
        
        
        return visited.get();
    }

    protected void buildExe(PackagerContext ctx, PackageFileVisitor visitor, SetupAppFactory<?> appFactory, AttributeKey attr, Optional<String> executableName) {
        var bldr = createBasicBuilder(executableName).
            withMain(appFactory.getClass());
        
        appFactory.app().decorateExecutable(bldr);
        
        runBuilder(ctx, visitor, attr, bldr);
    }
    
    protected void buildOtherExe(PackagerContext ctx, PackageFileVisitor visitor, Class<?> i, AttributeKey attr, Optional<String> executableName) {
        runBuilder(ctx, visitor, attr, 
    		createBasicBuilder(executableName).
            	withMain(i)
        );
    }

	private uk.co.bithatch.ninstall.lib.Executable.Builder createBasicBuilder(Optional<String> executableName) {
		return new Executable.Builder().
            withVerboseOutput(verboseOutput).
            withParallelism(parallelism).
            withExecutableName(executableName).
            withOptions(
                "--enable-http",
                "--enable-https"
            );
	}

	private void runBuilder(PackagerContext ctx, PackageFileVisitor visitor, AttributeKey attr,
			uk.co.bithatch.ninstall.lib.Executable.Builder bldr) {
		/* TODO just the jars we need */
        bldr.withClasspath(new JarPath.Builder().fromCurrentJarpath().build());
        
        var exe = bldr.build();
        var exePath = exe.generate(ctx.progress());
        if(attr != null)
            ctx.attributes().put(attr, exePath);
        
        IO.ioRun(() -> 
            visitor.visit(
                new OutputFileset.Builder(Locations.PROGRAMS).build(), 
                new InputFileset.Builder().
                    withBase(exePath.getParent()).
                    build(), exePath)
        );
	}
}
