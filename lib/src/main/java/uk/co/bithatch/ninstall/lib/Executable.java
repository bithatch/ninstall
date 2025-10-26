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
package uk.co.bithatch.ninstall.lib;

import org.apache.bcel.Repository;

import uk.co.bithatch.ninstall.lib.Resource.Source;

import static uk.co.bithatch.ninstall.lib.IO.displayPath;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Executable {
    
    public record BundledResource(String path, Path source) {
    	
    	static BundledResource of(Resource resource) {
    		if(resource.source() == Source.BUNDLED) {
    			throw new IllegalArgumentException("TODO.");
    		}
    		else
    			throw new IllegalArgumentException("Not a bundled resource.");
    	}
        
        static BundledResource of(Path source) {
            return new BundledResource(source.getFileName().toString(), source);
        }
    }


    public final static class Builder {
        private Class<?> mainClass;
        private Path graalHome = Paths.get(System.getProperty("java.home"));
        private String toolPath = "bin/native-image";
        private Optional<JarPath> classpath = Optional.empty();
        private List<Class<?>> classes = new ArrayList<>();
        private Optional<Path> outputFile = Optional.empty();
        private Optional<String> executableName = Optional.empty();
        private Set<BundledResource> resources = new LinkedHashSet<>();
        private boolean verboseOutput;
        private boolean executeBit = true;
        private List<String> options = new ArrayList<>();
        private Optional<Integer> parallelism = Optional.empty();
        
        public Builder withParallelism(int parallelism) {
        	return withParallelism(Optional.of(parallelism));
        }
        
        public Builder withParallelism(Optional<Integer> parallelism) {
        	this.parallelism = parallelism;
        	return this;
        }
        
        public Builder withOptions(String... options) {
            return withOptions(Arrays.asList(options));
        }
        
        public Builder withOptions(Collection<String> options) {
            this.options.addAll(options);
            return this;
        }
        
        public Builder withExecuteBit() {
            return withExecuteBit(true);
        }
        
        public Builder withExecuteBit(boolean executeBit) {
            this.executeBit = executeBit;
            return this;
        }

        public Builder withVerboseOutput() {
            return withVerboseOutput(true);
        }
        
        public Builder withVerboseOutput(boolean verboseOutput) {
            this.verboseOutput = verboseOutput;
            return this;
        }

        public Builder withRootResourcePath(Path resource) {
            return withRootResourcePaths(resource);
        }

        public Builder withRootResourcePaths(Path... resources) {
            return withRootResourcePaths(Arrays.asList(resources));
        }
        
        public Builder withRootResourcePaths(Collection<Path> resources) {
            return withResources(resources.stream().map(BundledResource::of).toList());
        }

        public Builder withResource(BundledResource resource) {
        	return withResources(resource);
        }
        
        public Builder withResources(BundledResource... resources) {
            return withResources(Arrays.asList(resources));
            
        }
        public Builder withResources(Collection<BundledResource> resources) {
            this.resources.addAll(resources);
            return this;
        }

        public Builder withExecutableName(String executableName) {
            return withExecutableName(Optional.of(executableName));
        }

        public Builder withExecutableName(Optional<String> executableName) {
            this.executableName = executableName;
            return this;
        }

        public Builder withOutputFile(String outputFile) {
            return withOutputFile(Paths.get(outputFile));
        }
        
        public Builder withOutputFile(Path outputFile) {
            return withOutputFile(Optional.of(outputFile));
        }
        
        public Builder withOutputFile(Optional<Path> outputFile) {
            this.outputFile = outputFile; 
            return this;
        }
        
        public Builder withMain(Class<?> mainClass) {
            this.mainClass = mainClass;
            return this;
        }
        public Builder withGraalHome(Path graalHome) {
            this.graalHome = graalHome;
            return this;
        }
        
        public Builder withToolPath(String toolPath) {
            this.toolPath = toolPath;
            return this;
        }
        
        public Builder withClasspath(JarPath classpath) {
            this.classpath = Optional.of(classpath);
            return this;
        }
        
        public Builder withClasses(Class<?>... classes) {
            return withClasses(Arrays.asList(classes));
        }
        
        public Builder withClasses(Collection<Class<?>> classes) {
            this.classes.clear();
            this.classes.addAll(classes);
            return this;
        }
        
        public Executable build() {
            return new Executable(this);
        }
    }
    
    private final Class<?> mainClass;
    private final List<Class<?>> classes;
    private final Path graalHome;
    private final String toolPath;
    private final Path outputFile;
    private final Optional<JarPath> classpath;
    private final Set<BundledResource> resources;
    private final boolean verboseOutput;
    private final boolean executeBit;
    private final List<String> options;
    private final Optional<Integer> parallelism;
    
    private Executable(Builder builder) {
        if(builder.mainClass == null)
            throw new IllegalArgumentException("Main class must be provided.");
        
        this.options = Collections.unmodifiableList(new ArrayList<>(builder.options));
        this.verboseOutput = builder.verboseOutput;
        this.executeBit = builder.executeBit;
        this.mainClass = builder.mainClass;
        this.classpath = builder.classpath;
        this.graalHome = builder.graalHome;
        this.toolPath = builder.toolPath;
        this.classes = Collections.unmodifiableList(new ArrayList<>(builder.classes));
        this.resources = Collections.unmodifiableSet(new LinkedHashSet<>(builder.resources));
        this.parallelism = builder.parallelism;
        
        if(builder.outputFile.isEmpty()) {
            try {
                if(builder.executableName.isPresent()) {
                    var dir = Files.createTempDirectory("ninstall");
                    outputFile = dir.resolve(builder.executableName.get());
                    dir.toFile().deleteOnExit(); /* TODO does this work on dirs? */                    
                }
                else {
                    outputFile = Files.createTempFile("ninstall", Machine.hostMachine().os().executable("native"));
                }
                outputFile.toFile().deleteOnExit();
            }
            catch(IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        }
        else {
            outputFile = builder.outputFile.get();
        }
    }
    
    public Path generate(Progress progress) {
        try {
            var tmp = Files.createTempDirectory("mvnpck");
    
            extractClass(mainClass, tmp);
            classes.forEach(clz -> extractClass(clz, tmp));
            
            var fullPathBldr = new JarPath.Builder();
            classpath.ifPresent(fullPathBldr::fromJarPath);
            fullPathBldr.addPaths(tmp);
            var fullPath = fullPathBldr.build();
            
    
            progress.info("native-image {0} -> {1}", mainClass.getName(), displayPath(outputFile));
            
            var toolExe = graalHome.resolve(toolPath);
            var args = new ArrayList<String>();
            args.addAll(Arrays.asList(
                toolExe.toString(), 
                mainClass.getName().toString(),
                outputFile.toAbsolutePath().toString(),
                "-classpath", fullPath.toString()
            ));
            
            args.addAll(options);

            args.add("-H:IncludeResources=META-INF/services/.*");
            
            resources.forEach(res -> {
                var out = tmp.resolve(res.path);
                try {
                    Files.createDirectories(out.getParent());
                    Files.copy(res.source, out);
                    args.add("-H:IncludeResources=" + res.path.replace(".", "\\."));
                    progress.info(" +{0} -> {1}", res.path, out);
                }
                catch(IOException ioe) {
                    throw new UncheckedIOException(ioe);
                }
            });
            
            parallelism.ifPresent(p -> args.add("--parallelism=" + p));
            
            var pb = new ProcessBuilder(args);
            pb.redirectError(Redirect.INHERIT);
            pb.redirectInput(Redirect.INHERIT);
            if(verboseOutput)
                pb.redirectOutput(Redirect.INHERIT);
            else
                pb.redirectOutput(Redirect.DISCARD);
            pb.directory(tmp.toFile());
            progress.command("{0}", displayPath(Paths.get(args.get(0))), String.join(" ", args.subList(1,  args.size())));
            var pr = pb.start();            
            try {
                if (pr.waitFor() != 0)
                    throw new IOException("Link failed with exit code " + pr.exitValue());
            } catch (InterruptedException ie) {
                throw new IOException("Interrupted.");
            }
    
            /* Clean up tmp */
            IO.delete(tmp);
            
            if(executeBit) {
                outputFile.toFile().setExecutable(true, false);
            }
            
            return outputFile;
        }
        catch(IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    private void extractClass(Class<?> clazz, Path tmp) {
        try {
            var path = tmp.resolve(clazz.getName().replace(".", "/") + ".class");
            Files.createDirectories(path.getParent());
            try (var out = Files.newOutputStream(path)) {
                var sx = Repository.lookupClass(clazz);
                sx.dump(out);
            } catch (ClassNotFoundException e) {
                throw new IOException("Failed to find self extractor.", e);
            }
        }
        catch(IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }
}
