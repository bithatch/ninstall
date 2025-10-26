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

import static uk.co.bithatch.ninstall.lib.IO.checkParentDir;
import static uk.co.bithatch.ninstall.lib.IO.displayPath;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import uk.co.bithatch.ninstall.lib.DisplayMode;
import uk.co.bithatch.ninstall.lib.IO;
import uk.co.bithatch.ninstall.lib.Mode;
import uk.co.bithatch.ninstall.lib.Where.Layout;

public class DirectoryPackager extends AbstractInstallablePackager {

    public final static class Builder extends AbstractInstallablePackager.Builder<DirectoryPackager, Builder> {

    	{
    		withInstallerName("installer");
    	}
    	
        @Override
        public DirectoryPackager build() {
            return new DirectoryPackager(this);
        }

    }

    private DirectoryPackager(Builder builder) {
        super(builder);
    }

    @Override
    public String extension() {
        return "";
    }

    @Override
    protected Package makeImpl(PackagerContext ctx) {
        if(!Files.exists(ctx.output())) {
            ctx.progress().command("mkdir {0}", displayPath(ctx.output()));
            IO.ioCall(() -> Files.createDirectories(ctx.output()));
        }
        visit(ctx, (output, input, path) -> {
            var out = output.resolve(path, input.base(), ctx.target(), Layout.FLAT);
            var absOut = ctx.output().resolve(out);
                ctx.progress().command("cp {0} {1}", displayPath(path), displayPath(absOut));
           		Files.copy(path, checkParentDir(absOut), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        });
        
        return new Package() {

            @Override
            public Path location() {
                return ctx.output();
            }

            @Override
            public void install(Mode mode, DisplayMode display) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

}
