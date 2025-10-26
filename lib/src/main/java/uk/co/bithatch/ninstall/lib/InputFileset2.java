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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import io.github.azagniotov.matcher.AntPathMatcher;

public final class InputFileset2 {

	public final static InputFileset2 executable(String path) {
		return executable(path, Machine.hostMachine().os());
	}
	
	public final static InputFileset2 executable(String path, OperatingSystem os) {
		return include(os.executable(path));
	}
	
	public final static InputFileset2 include(String... path) {
		return new Builder().include(path).build();
	}
	
	public final static InputFileset2 exclude(String... path) {
		return new Builder().include(path).build();
	}

    public final static class Builder {

        private Optional<Path> base = Optional.empty();
        private final List<String> include = new ArrayList<>();
        private final List<String> exclude = new ArrayList<>();

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
            this.include.addAll(paths);
            return this;
        }

        public Builder exclude(String... paths) {
            return exclude(Arrays.asList(paths));
        }

        public Builder exclude(Collection<String> paths) {
            this.exclude.addAll(paths);
            return this;
        }

        public InputFileset2 build() {
            return new InputFileset2(this);
        }
    }

    private final Optional<Path> base;
    private final List<String> include;
    private final List<String> exclude;

    private InputFileset2(Builder builder) {
        this.base = builder.base;
        this.include = Collections.unmodifiableList(new ArrayList<>(builder.include));
        this.exclude = Collections.unmodifiableList(new ArrayList<>(builder.exclude));
    }

    public Optional<Path> base() {
        return base;
    }

    public List<String> include() {
        return include;
    }

    public List<String> exclude() {
        return exclude;
    }
    
    public Stream<Path> paths(Path defaultBase) {
        var matcher = new AntPathMatcher.Builder().build();
        try {
        	var base = this.base.orElse(defaultBase);
            return Files.walk(base).filter(path -> {
                if(path.equals(base))
                    return false;
                var relPath = base.relativize(path);
                if (!exclude.isEmpty()) {
                    for (var x : exclude) {
                        if (matcher.isMatch(x, relPath.toString())) {
                            return false;
                        }
                    }
                }
                if (include.isEmpty())
                    return true;
                else {
                    for (var i : include) {
                        if (matcher.isMatch(i, relPath.toString())) {
                            return true;
                        }
                    }
                }
                return false;
            });
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }

    }

}
