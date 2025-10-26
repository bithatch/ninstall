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
package uk.co.bithatch.ninstall.lib.steps;

import java.nio.file.Path;
import java.nio.file.Paths;

import uk.co.bithatch.ninstall.lib.StepContext;

public final class RemoveDirectory extends AbstractRemoveDirectory<StepContext<?>> {

    public final static class Builder {
        private Path path;
        private boolean recursive;
        private boolean failIfMissing;
        private boolean failIfNotEmpty = true;

        public Builder withFailIfMissing() {
            return withFailIfMissing(true);
        }
        
        public Builder withFailIfMissing(boolean failIfMissing) {
            this.failIfMissing = failIfMissing;
            return this;
        }

        public Builder withoutFailIfNotEmpty() {
            return withFailIfMissing(true);
        }
        
        public Builder withFailIfNotEmpty(boolean failIfNotEmpty) {
            this.failIfNotEmpty = failIfNotEmpty;
            return this;
        }

        public Builder withRecursive() {
            return withRecursive(true);
        }
        
        public Builder withRecursive(boolean recursive) {
            this.recursive = recursive;
            return this;
        }
        
        public Builder withPath(String path) {
            return withPath(Paths.get(path));
        }

        public Builder withPath(Path path) {
            this.path = path;
            return this;
        }

        public RemoveDirectory build() {
            return new RemoveDirectory(this);
        }

    }

    private Path path;

    private RemoveDirectory(Builder builder) {
        super(builder.recursive, builder.failIfMissing, builder.failIfNotEmpty);
        this.path = builder.path;
    }

    @Override
    protected Path getPath(StepContext<?> context) {
        return context.setup().resolve(path);
    }
}
