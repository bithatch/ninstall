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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class JarPath {

    public final static class Builder {

        private Set<Path> paths = new LinkedHashSet<>();

        public Builder fromCurrentClasspath() {
            return fromString(System.getProperty("java.class.path"));
        }

        public Builder fromCurrentModulePath() {
            return fromString(System.getProperty("jdk.module.path"));
        }

        public Builder fromCurrentJarpath() {
            paths.clear();
            addFromString(System.getProperty("java.class.path"));
            addFromString(System.getProperty("jdk.module.path"));
            return this;
        }

        public Builder fromString(String str) {
            paths.clear();
            return addFromString(str);
        }
        
        public Builder fromJarPath(JarPath path) {
            return fromPaths(path.paths());
        }
        
        public Builder fromPaths(Collection<Path> paths) {
            this.paths.clear();
            return addPaths(paths);
        }
        
        public Builder fromPaths(Path... paths) {
            this.paths.clear();
            return addPaths(paths);
        }
        
        public Builder addPaths(Collection<Path> paths) {
            this.paths.addAll(paths);
            return this;
        }
        
        public Builder addPaths(Path... paths) {
            return addPaths(Arrays.asList(paths));
        }

        public Builder addFromString(String str) {
            if(str == null)
                return this;
            return addPaths(Arrays.asList(str.split(File.pathSeparator)).stream().map(Paths::get).toList());
        }

        public JarPath build() {
            return new JarPath(this);
        }
    }

    private final Set<Path> paths;

    private JarPath(Builder bldr) {
        this.paths = Collections.unmodifiableSet(new LinkedHashSet<>(bldr.paths));
    }

    public Set<Path> paths() {
        return paths;
    }

    @Override
    public String toString() {
        return String.join(File.pathSeparator, paths.stream().map(Path::toString).toList());
    }
}
