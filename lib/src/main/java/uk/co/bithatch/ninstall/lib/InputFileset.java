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
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.github.azagniotov.matcher.AntPathMatcher;

public final class InputFileset {
	
	@FunctionalInterface
	public interface FilesetCondition {
		boolean evaluate(InputFileset fileset);
	}
	
	public static FilesetCondition and(FilesetCondition... conditions) {
		return is -> {
			for(var condition : conditions) {
				if(!condition.evaluate(is)) {
					return false;
				}
			}
			return true;
		};
	}
	
	public static FilesetCondition or(FilesetCondition... conditions) {
		return is -> {
			for(var condition : conditions) {
				if(condition.evaluate(is)) {
					return true;
				}
			}
			return false;
		};
	}

	public static FilesetCondition not(FilesetCondition condition) {
		return is -> !condition.evaluate(is);
	}
	
	public final static FilesetCondition ALWAYS = p -> true;

	public final static InputFileset executable(String path) {
		return executable(path, Machine.hostMachine().os());
	}

	public final static InputFileset executable(String path, OperatingSystem os) {
		return include(os.executable(path));
	}

	public final static InputFileset include(String... path) {
		return new Builder().include(path).build();
	}

	public final static InputFileset includeWhen(FilesetCondition when,  String... path) {
		return new Builder().
				include(path).
				condition(when).
				build();
	}

	public final static InputFileset exclude(String... path) {
		return new Builder().include(path).build();
	}

	public final static InputFileset excludeWhen(FilesetCondition when,  String... path) {
		return new Builder().
				exclude(path).
				condition(when).
				build();
	}

	public final static class Builder {

		private Optional<Path> base = Optional.empty();
		private Optional<Predicate<Path>> include = Optional.empty();
		private Optional<Predicate<Path>> exclude = Optional.empty();
		private Optional<FilesetCondition> condition = Optional.empty();

		public Builder withBase(String base) {
			return withBase(Paths.get(base));
		}

		public Builder withBase(File base) {
			return withBase(base.toPath());
		}

		public Builder withBase(Path base) {
			this.base = Optional.of(base);
			return this;
		}

		public Builder include(String... paths) {
			return include(Arrays.asList(paths));
		}

		public Builder include(Collection<String> paths) {
			var matcher = new AntPathMatcher.Builder().build();
			return include(p -> matches(matcher, p, paths));
		}

		private boolean matches(AntPathMatcher matcher, Path p, Collection<String> paths) {
			for (var i : paths) {
				if (matcher.isMatch(i, p.toString())) {
					return true;
				}
			}
			return false;
		}

		public Builder include(Predicate<Path> include) {
			this.include = Optional.of(include);
			return this;
		}

		public Builder exclude(String... paths) {
			return exclude(Arrays.asList(paths));
		}

		public Builder exclude(Collection<String> paths) {
			var matcher = new AntPathMatcher.Builder().build();
			return exclude(p -> matches(matcher, p, paths));
		}

		public Builder exclude(Predicate<Path> exclude) {
			this.exclude = Optional.of(exclude);
			return this;
		}
		
		public Builder condition(FilesetCondition condition) {
			this.condition = Optional.of(condition);
			return this;
		}

		public InputFileset build() {
			return new InputFileset(this);
		}
	}

	private final Optional<Path> base;
	private final Optional<Predicate<Path>> include;
	private final Optional<Predicate<Path>> exclude;
	private final Optional<FilesetCondition> condition;

	private InputFileset(Builder builder) {
		this.base = builder.base;
		this.include = builder.include;
		this.exclude = builder.exclude;
		this.condition = builder.condition;
	}

	public Optional<FilesetCondition> condition() {
		return condition;
	}

	public Optional<Path> base() {
		return base;
	}

	public Optional<Predicate<Path>>  include() {
		return include;
	}

	public Optional<Predicate<Path>>  exclude() {
		return exclude;
	}

	public Stream<Path> paths(Path defaultBase) {
		
		if(condition.isPresent() && !condition.get().evaluate(this)) {
			return Stream.empty();
		}
		
		try {
			var base = this.base.orElse(defaultBase);
			return Files.walk(base).filter(path -> {
				if (path.equals(base))
					return false;
				var relPath = base.relativize(path);
				if (!exclude.isEmpty()) {
					if(exclude.get().test(relPath)) {
						return false;
					}
				}
				
				return include.map(i -> i.test(relPath)).orElse(true);
			});
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}

	}

}
