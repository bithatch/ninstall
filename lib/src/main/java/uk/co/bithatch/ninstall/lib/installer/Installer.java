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
package uk.co.bithatch.ninstall.lib.installer;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.sshtools.liftlib.IElevator;
import com.sshtools.liftlib.OS;

import uk.co.bithatch.ninstall.lib.AppAttribute;
import uk.co.bithatch.ninstall.lib.AttributeKey;
import uk.co.bithatch.ninstall.lib.DirectoryAnalysis;
import uk.co.bithatch.ninstall.lib.IO;
import uk.co.bithatch.ninstall.lib.InstallResult;
import uk.co.bithatch.ninstall.lib.Machine;
import uk.co.bithatch.ninstall.lib.Mode;
import uk.co.bithatch.ninstall.lib.Registry;
import uk.co.bithatch.ninstall.lib.Scope;
import uk.co.bithatch.ninstall.lib.SetupApp;
import uk.co.bithatch.ninstall.lib.SetupAppOptions;
import uk.co.bithatch.ninstall.lib.DirectoryAnalysis.Results;

public final class Installer extends SetupApp<InstallationContext, InstallerToolkit> {

    public final static class Builder extends SetupApp.Builder<Installer, Builder, InstallationContext> {
        private String name;
        private Optional<Path> installPath = Optional.empty();
        private Optional<Path> sourceLocation = Optional.empty();
        
        public Builder(String name) {
            this.name = name;
        }
        
        public Builder withInstallPath(String path) {
            return withInstallPath(Paths.get(path));
        }

        public Builder withInstallPath(Path installPath) {
            this.installPath = Optional.of(installPath);
            return this;
        }
        
        public Builder withSourceLocation(String path) {
            return withSourceLocation(Paths.get(path));
        }

        public Builder withSourceLocation(Path sourcePath) {
            this.sourceLocation = Optional.of(sourcePath);
            return this;
        }

        public Installer build() {
            return new Installer(this);
        }
    }

    private final String name;
    private final Optional<Path> installPath;
    private final Path sourceLocation;

    private Installer(Builder bldr) {
        super(bldr, InstallerToolkit.class);
        this.name = bldr.name;
        this.installPath = bldr.installPath;
        this.sourceLocation = bldr.sourceLocation.orElseGet(IO::cwd);
    }
    
    public String name() {
        return name;
    }

    public Optional<Path> installPath() {
        return installPath;
    }

    @Override
    protected void afterSetup(InstallResult result) {
        if(result == InstallResult.INSTALLED)
            AppAttribute.save(context().installLocation(), context().attributes());
    }

    @Override
    protected InstallationContext createContext(Scope scope, Registry registry, SetupAppOptions options, Mode mode) {
        return new InstallationContext() {
            
            protected final Map<AttributeKey, Object> attrs = new HashMap<>();
            
            private Results analysis;
            
            {
                var defaultInstallPath = Machine.hostMachine().os().installRoot(Scope.current()).resolve(name());
                attrs.put(InstallerAttribute.DEFAULT_INSTALL_LOCATION, defaultInstallPath);
                attrs.put(InstallerAttribute.INSTALL_LOCATION, registry.get(appId()).orElse(defaultInstallPath));
                if(OS.isAdministrator()) {
                    attrs.put(InstallerAttribute.ROOT_DIRECTORY, Paths.get(File.separator));
                }
                else {
                    attrs.put(InstallerAttribute.ROOT_DIRECTORY, Paths.get(System.getProperty("user.home")));
                }
                attrs.put(AppAttribute.NAME, name());
            }

            @Override
            public Map<AttributeKey, Object> attributes() {
                return attrs;
            }

            @Override
            public Installer setupApp() {
                return Installer.this;
            }

            @Override
            public Path installLocation() {
                var path = (Path)attrs.get(InstallerAttribute.INSTALL_LOCATION);
                if(path == null)
                    throw new IllegalStateException("Install location has not been set.");
                return path;
            }

            @Override
            public Results archiveAnalysis() {
                if(analysis == null) {
                    try {
                        analysis = new DirectoryAnalysis(sourceLocation()).analyse(sourcesFilter());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
                return analysis;
            }

            private Predicate<? super Path> sourcesFilter() {
            	
            	var thisExe = Paths.get(ProcessHandle.current().info().command().orElseThrow(
                        () -> new IllegalStateException("Could not determine own executable."))
                        );
            	
				return p -> {
	                if(p.getFileName().equals(thisExe.getFileName())) {
	                    return false;
	                }
					return !p.equals(sourceLocation);
				};
			}

			@Override
            public Path sourceLocation() {
                return sourceLocation;
            }

            @Override
            public Registry registry() {
                return registry;
            }

            @Override
            public Scope scope() {
                return scope;
            }

            @Override
            public SetupAppOptions options() {
                return options;
            }

            @Override
            public List<AttributeKey> keys() {
                return Arrays.asList(InstallerAttribute.values());
            }

            @Override
            public Mode mode() {
                return mode;
            }

			@Override
			public IElevator elevator() {
				return Installer.this.elevator();
			}
        };
    }



}
