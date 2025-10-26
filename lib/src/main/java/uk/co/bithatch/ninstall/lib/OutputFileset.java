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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import uk.co.bithatch.ninstall.lib.Where.Layout;

public final class OutputFileset {

    public final static class Builder {

        private final Where where;
        private final List<InputFileset> input = new ArrayList<>();
        
        private boolean preservePath;
        private Optional<Path> base = Optional.empty();
        private Optional<Path> path = Optional.empty();

        public Builder(Where where) {
            this.where = where;
        }

        public Builder withPreservePath() {
            return withPreservePath(true);
        }

        public Builder withPreservePath(boolean preservePath) {
            this.preservePath = preservePath;
            return this;
        }

        public Builder withInput(InputFileset... input) {
            return withInput(Arrays.asList(input));
        }

        public Builder withInput(Collection<InputFileset> input) {
            this.input.addAll(input);
            return this;
        }

        public Builder withInputBase(String base) {
        	return withInputBase(Paths.get(base));
        }
        
        public Builder withInputBase(File base) {
        	return withInputBase(base.toPath());
        }
        
        public Builder withInputBase(Path base) {
        	this.base = Optional.of(base);
        	return this;
        }
        
        public Builder withPath(String path) {
        	return withPath(Paths.get(path));
        }
        
        public Builder withPath(Path path) {
        	this.path = Optional.of(path);
        	return this;
        }

        public OutputFileset build() {
            return new OutputFileset(this);
        }
    }

    private final List<InputFileset> input;
    private final Where where;
    private final boolean preservePath;
    private final Optional<Path> inputBase;
    private final Optional<Path> path;

    private OutputFileset(Builder bldr) {
        this.input = Collections.unmodifiableList(bldr.input);
        this.where = bldr.where;
        this.preservePath = bldr.preservePath;
        this.inputBase = bldr.base;
        this.path = bldr.path;
    }

    public Optional<Path> inputBase() {
        return inputBase;
    }

    public Optional<Path> path() {
        return path;
    }

    public Where where() {
        return where;
    }

    public List<InputFileset> input() {
        return input;
    }

    public Path resolve(Path child, Optional<Path> base, Machine machine, Layout layout) {
        var dir = path.map(p -> {
        	if(p.isAbsolute()) {
        		return p;
        	}
        	else {
        		return where.resolve(machine, layout).resolve(p);
        	}
        }).orElseGet(() -> where.resolve(machine, layout));
        
        if(preservePath) {
            return dir.resolve(base.orElseGet(() -> inputBase.orElseGet(IO::cwd)).
           			toAbsolutePath().relativize(child.toAbsolutePath())
            );
        }
        else {
            return dir.resolve(child.getFileName());
        }
    }

}
