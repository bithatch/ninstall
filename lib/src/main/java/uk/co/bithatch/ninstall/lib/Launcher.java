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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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

import uk.co.bithatch.ninstall.lib.Where.Layout;

public interface Launcher {

	public abstract static class AbstractBuilder<LNCHR extends Launcher, BLDR extends AbstractBuilder<LNCHR, BLDR>> {
		final String name;
		final List<String> arguments = new ArrayList<>();

		protected AbstractBuilder(String name) {
			this.name = name;
		}

		public BLDR withArguments(String... arguments) {
			return withArguments(Arrays.asList(arguments));
		}

		@SuppressWarnings("unchecked")
		public BLDR withArguments(Collection<String> arguments) {
			this.arguments.addAll(arguments);
			return (BLDR) this;
		}

		public abstract LNCHR build();
	}

	public final static class JavaLauncherBuilder extends AbstractBuilder<JavaLauncher, JavaLauncherBuilder> {

		private Optional<String> module = Optional.empty();
		private Optional<String> main = Optional.empty();
		private Optional<Path> mainJar = Optional.empty();
		private Optional<Path> jarbase = Optional.empty();
		private Set<InputFileset> classpath = new LinkedHashSet<>();
		private Set<InputFileset> modulepath = new LinkedHashSet<>();
		private final List<String> vmArguments = new ArrayList<>();

		public JavaLauncherBuilder(String name) {
			super(name);
		}
		
		public JavaLauncherBuilder withModule(String module) {
			this.module = Optional.of(module);
			return this;
		}
		
		public JavaLauncherBuilder withMain(String main) {
			this.main = Optional.of(main);
			return this;
		}

		public JavaLauncherBuilder withMainJar(String mainJar) {
			return withMainJar(Paths.get(mainJar));
		}
		
		public JavaLauncherBuilder withMainJar(Path mainJar) {
			this.mainJar = Optional.of(mainJar);
			return this;
		}

		public JavaLauncherBuilder withClasspath(InputFileset... paths) {
			return withClasspath(Arrays.asList(paths));
		}

		public JavaLauncherBuilder withClasspath(Collection<InputFileset> paths) {
			this.classpath.addAll(paths);
			return this;
		}

		public JavaLauncherBuilder withModulepath(InputFileset... paths) {
			return withModulepath(Arrays.asList(paths));
		}

		public JavaLauncherBuilder withModulepath(Collection<InputFileset> paths) {
			this.modulepath.addAll(paths);
			return this;
		}

		public JavaLauncherBuilder withJarBase(String base) {
			return withJarBase(Paths.get(base));
		}

		public JavaLauncherBuilder withJarBase(File base) {
			return withJarBase(base.toPath());
		}

		public JavaLauncherBuilder withJarBase(Path base) {
			this.jarbase = Optional.of(base);
			return this;
		}


		public JavaLauncherBuilder withVmArguments(String... arguments) {
			return withVmArguments(Arrays.asList(arguments));
		}

		public JavaLauncherBuilder withVmArguments(Collection<String> arguments) {
			this.arguments.addAll(arguments);
			return this;
		}

		@Override
		public JavaLauncher build() {
			return new JavaLauncher(this);
		}

	}

	public final static class JavaLauncher implements Launcher {

		private final String name;
		private final List<String> arguments;
		private final List<String> vmArguments;
		private final Optional<String> module;
		private final Optional<String> main;
		private final Optional<Path> mainJar;
		private final Optional<Path> jarbase;
		private final Set<InputFileset> classpath;
		private final Set<InputFileset> modulepath;

		private JavaLauncher(JavaLauncherBuilder bldr) {
			this.name = bldr.name;
			this.arguments = Collections.unmodifiableList(new ArrayList<>(bldr.arguments));
			this.vmArguments = Collections.unmodifiableList(new ArrayList<>(bldr.vmArguments));
			this.module = bldr.module;
			this.main = bldr.main;
			this.mainJar = bldr.mainJar;
			this.jarbase = bldr.jarbase;
			this.classpath = Collections.unmodifiableSet(new LinkedHashSet<>(bldr.classpath));
			this.modulepath = Collections.unmodifiableSet(new LinkedHashSet<>(bldr.modulepath));
		}

		@Override
		public String name() {
			return name;
		}

		public List<String> arguments() {
			return arguments;
		}

		public Optional<String> module() {
			return module;
		}

		public Optional<String> main() {
			return main;
		}

		public Optional<Path> mainJar() {
			return mainJar;
		}

		public Optional<Path> jarbase() {
			return jarbase;
		}

		public Set<InputFileset> classpath() {
			return classpath;
		}

		public Set<InputFileset> modulepath() {
			return modulepath;
		}

		@Override
		public void create(Stash stash, Journal journal, SetupAppContext<?, ?> context) throws IOException {
			var installdir = context.installLocation();
			var conf = installdir.resolve(Locations.CONFIGURATION.resolve(Layout.FLAT));
			var jarbase = this.jarbase.orElse(installdir);
			
			Files.createDirectories(conf);
			
			var launcherprops = conf.resolve(name + ".argfile");
			try(var wtr = new PrintWriter(Files.newBufferedWriter(launcherprops))) {
				
				/* VM arguments */
				vmArguments.forEach(wtr::println);
				
				/* Classpath */
				var paths = classpath.stream().flatMap(cp -> cp.paths(jarbase)).toList();
				if(!paths.isEmpty()) {
					wtr.println("-cp");
					wtr.println(String.join(File.pathSeparator, paths.stream().map(p -> p.toAbsolutePath().toString()).toList()));
				}
				
				/* Modulepath*/
				paths = modulepath.stream().flatMap(cp -> cp.paths(jarbase)).toList();
				if(!paths.isEmpty()) {
					wtr.println("-p");
					wtr.println(String.join(File.pathSeparator, paths.stream().map(p -> p.toAbsolutePath().toString()).toList()));
				}
				
				/* Main */
				mainJar.ifPresentOrElse(mj -> {
					wtr.println("-jar");
					wtr.println(mj);
				}, () -> {
					module.ifPresentOrElse(mod -> {
						wtr.println("-m");
						wtr.println(mod + "/" + main.orElseThrow(() -> new IllegalStateException("If a module is provided, a main class must be provided.")));
					}, () -> {
						wtr.println(main.orElseThrow(() -> new IllegalStateException("No main jar or module, so a main class must be provided.")));
					});
				});
				wtr.println();
				
				/* Program arguments */
				arguments.forEach(wtr::println);
			}
			
			journal.log(launcherprops);
		}
	}

	String name();

	void create(Stash stash, Journal journal, SetupAppContext<?, ?> context) throws IOException;

}
