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

import static uk.co.bithatch.ninstall.lib.IO.displayPath;
import static uk.co.bithatch.ninstall.lib.IO.ioRun;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import uk.co.bithatch.ninstall.lib.Archives;
import uk.co.bithatch.ninstall.lib.DisplayMode;
import uk.co.bithatch.ninstall.lib.Executable;
import uk.co.bithatch.ninstall.lib.IO;
import uk.co.bithatch.ninstall.lib.Machine;
import uk.co.bithatch.ninstall.lib.Mode;
import uk.co.bithatch.ninstall.lib.SelfExtractor;
import uk.co.bithatch.ninstall.lib.Where.Layout;

public class SelfExtractingExecutablePackager extends AbstractInstallablePackager {

    public final static class Builder extends AbstractInstallablePackager.Builder<SelfExtractingExecutablePackager, Builder> {

        @Override
        public SelfExtractingExecutablePackager build() {
            return new SelfExtractingExecutablePackager(this);
        }
    }

    private SelfExtractingExecutablePackager(Builder builder) {
        super(builder);
    }

    @Override
    public String extension() {
        return Machine.hostMachine().os().executable("");
    }

    @Override
    protected Package makeImpl(PackagerContext ctx) {
        var mf = ctx.manifest();
        
        ioRun(() -> {
            
            var properties = new Properties();
            properties.put(SelfExtractor.NAME, mf.name());
            properties.put(SelfExtractor.VERSION, mf.version());
            
            var tmp = Files.createTempDirectory("frk");

            /* Zip of image */
            var data = tmp.resolve("data.zip");
            
            try(var zos = new ZipOutputStream(Files.newOutputStream(data))) {
                ctx.progress().command("sx {0}", displayPath(ctx.output()));
                visit(ctx, (output, input, path) -> {
                    var out = output.resolve(path, input.base(), ctx.target(), Layout.FLAT);
                    var outpath = out.toString() + (Files.isDirectory(path) ? "/" : "");
                    ctx.progress().info(" +{0}", displayPath(out));
					var entry = new ZipEntry(outpath);
                    Archives.putNextEntry(zos, entry, path);
                    if(!Files.isDirectory(path)) {
	                    try(var in = Files.newInputStream(path)) {
	                        in.transferTo(zos);
	                    }
                    }
                }); 
            }
            
            Path installerPath = (Path)ctx.attributes().get(PackagerAttributes.INSTALLER_PATH);
            if(installerPath != null) {
                properties.put(SelfExtractor.EXEC, installerPath.getFileName().toString());
            }

            /* Properties containing launcher script */
            var props = tmp.resolve("data.properties");
            try (var w = Files.newBufferedWriter(props)) {
                properties.store(w, "Sfx");
            }
            
            new Executable.Builder().
                withOptions(
                    "--enable-http",
                    "--enable-https"
                ).
                withMain(SelfExtractor.class).
                withOutputFile(ctx.output()).
                withVerboseOutput(verboseOutput).
                withParallelism(parallelism).
                withRootResourcePaths(data, props).
                build().
                generate(ctx.progress());

            /* Clean up tmp */
            IO.delete(tmp);
            
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
                ioRun(() -> Files.delete(location()));
            }
        };
    }
}
