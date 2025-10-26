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

import static uk.co.bithatch.ninstall.lib.IO.displayPath;
import static uk.co.bithatch.ninstall.lib.IO.ioRun;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import uk.co.bithatch.ninstall.lib.AttributeKey;
import uk.co.bithatch.ninstall.lib.BuildContext;
import uk.co.bithatch.ninstall.lib.IO;
import uk.co.bithatch.ninstall.lib.InputFileset;
import uk.co.bithatch.ninstall.lib.Machine;
import uk.co.bithatch.ninstall.lib.Manifest;
import uk.co.bithatch.ninstall.lib.OutputFileset;
import uk.co.bithatch.ninstall.lib.Progress;

public abstract class Packager {
    
    @FunctionalInterface
    public interface PackageFileVisitor {
        void visit(OutputFileset output, InputFileset input, Path path) throws IOException;
    }
    
    public final static String DEFAULT_FILENAME_PATTERN = "${package.name}-${package.version}-${package.os}_${package.arch}${package.ext}";

    @SuppressWarnings("unchecked")
    public static abstract class Builder<PKGR extends Packager, BLDR extends Builder<PKGR, BLDR>> {
        private Optional<Path> location = Optional.empty();
        private Optional<Machine> target = Optional.empty();
        private Optional<String> filenamePattern = Optional.empty();
        private Optional<Runnable> preInstall = Optional.empty();
        private Optional<Runnable> postInstall = Optional.empty();
        private boolean verboseOutput;
        private boolean clean = true;

        public BLDR withoutClean() {
        	return withClean(false);
        }
        
        public BLDR withClean(boolean clean) {
        	this.clean = clean;
        	return (BLDR)this;
        }
        public BLDR withVerboseOutput(boolean verboseOutput) {
            this.verboseOutput = verboseOutput;
            return (BLDR)this;
        }
        
        public BLDR withLocation(Path location) {
            this.location = Optional.of(location);
            return (BLDR)this;
        }
        
        public BLDR withFilenamePattern(String filenamePatter) {
            this.filenamePattern = Optional.of(filenamePatter);
            return (BLDR)this;
        }
        
        public BLDR withTarget(Machine target) {
            this.target = Optional.of(target);
            return (BLDR)this;
        }
        
        public BLDR withPreInstall(Runnable preInstall) {
            checkExecutableTask(preInstall.getClass());
            this.preInstall = Optional.of(preInstall);
            return (BLDR)this;
        }
        
        public BLDR withPostInstall(Runnable postInstall) {
            checkExecutableTask(postInstall.getClass());
            this.postInstall = Optional.of(postInstall);
            return (BLDR)this;
        }
        
        public abstract PKGR build();
    }
    
    private final Optional<Path> location;
    private final Optional<Machine> target;
    private final String filenamePattern;
    protected final boolean verboseOutput;
    protected final boolean clean;
    
    protected Packager(Builder<?,?> bldr) {
        this.verboseOutput = bldr.verboseOutput;
        this.location = bldr.location;
        this.target = bldr.target;
        this.filenamePattern = bldr.filenamePattern.orElse(DEFAULT_FILENAME_PATTERN);
        this.clean = bldr.clean;
    }
    
    public Package make(Manifest manifest) {
        return make(manifest, Progress.defaultProgress(), BuildContext.defaultContext());
    }

    public Package make(Manifest manifest, Progress progress, BuildContext ctx) {
        var dir = location.orElseGet(ctx::defaultOutput);
        if(Files.exists(dir)) {
            if(!Files.isDirectory(dir))
                throw new IllegalArgumentException(MessageFormat.format("Output location ''{0}'' must either not exist or be an existing directory", dir));
        }
        else {
            try {
                progress.command("mkdir {0}", dir);
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        
        var target = this.target.orElseGet(ctx::host);
        
        var pkgFile = dir.resolve(filenamePattern.
            replace("${package.name}", manifest.name()).
            replace("${package.version}", manifest.version()).
            replace("${package.os}", target.os().name().toLowerCase()).
            replace("${package.arch}", target.arch().name().toLowerCase()).
            replace("${package.ext}", extension()));
        
        if(clean && Files.exists(pkgFile)) {
            progress.command("rm {0}", displayPath(pkgFile));
            IO.delete(pkgFile);
        }
        
        return makeImpl(new PackagerContext() {
            
            private final Map<AttributeKey, Object> attrs = new HashMap<>();

            @Override
            public boolean verbose() {
                return verboseOutput;
            }
            
            @Override
            public Machine target() {
                return target;
            }
            
            @Override
            public Progress progress() {
                return progress;
            }
            
            @Override
            public Path output() {
                return pkgFile;
            }
            
            @Override
            public Manifest manifest() {
                return manifest;
            }
            
            @Override
            public BuildContext build() {
                return ctx;
            }

            @Override
            public Map<AttributeKey, Object> attributes() {
                return attrs;
            }
        });
    }
    
    public abstract String extension();
    
    protected abstract Package makeImpl(PackagerContext ctx);
    
    protected Path calcLocation(Supplier<Path> defaultLocation) {
        return location.orElseGet(defaultLocation);
    } 
    
    protected int visit(PackagerContext ctx, PackageFileVisitor visitor) {
    	var visited = new AtomicInteger(); 
        ctx.manifest().output().forEach(output -> {
            output.input().forEach(input -> {
                input.paths(output.inputBase().orElseGet(IO::cwd)).forEach(path -> {
                    ioRun(() -> visitor.visit(output,  input,  path));
                    visited.incrementAndGet();
                });
            });
        });
        return visited.get();
    }

    public static void checkExecutableTask(Class<?> clazz) {
//        boolean isStatic = Modifier.isStatic(clazz.getModifiers());
//        if(!clazz.isMemberClass() && !isStatic)
//            throw new IllegalArgumentException("Tasks must be a top level class or static");
        try {
            var method = clazz.getMethod("main", String[].class);
            if(!Modifier.isPublic(method.getModifiers())) {
                throw new IllegalArgumentException("Main method of task is not public.");
            }
            if(!Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException("Main method of task is not static.");
            }
            if(!method.getReturnType().equals(void.class)) {
                throw new IllegalArgumentException("Main method of task does not return void.");
            }
        }
        catch(Exception e) {
            throw new IllegalArgumentException("Post-installer must have a static main method, like an ordinary main Java class.", e);   
        }
    }
}
