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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class DirectoryAnalysis {

    private Path path;

    public record Results(Path path, int files, int entries, long totalSize, Predicate<? super Path> filter) {
    }

    public DirectoryAnalysis(Path path) {
        this.path = path;
    }

    public Results analyse(Predicate<? super Path> filter) throws IOException {
        var files = new AtomicInteger();
        var totalSize = new AtomicLong();
        var entries = new AtomicInteger();

        try (var stream = Files.walk(path, Integer.MAX_VALUE)) {
            stream.filter(filter).forEach(p -> {
            	entries.incrementAndGet();
                if (!Files.isDirectory(p)) {
                    files.incrementAndGet();
                    totalSize.addAndGet(IO.ioCall(() -> Files.size(p)));
                }
            });
        }

        return new Results(path, files.get(), entries.get(), totalSize.get(), filter);
    }
}
