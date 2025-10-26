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
package uk.co.bithatch.ninstall.lib.updater.steps;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

import uk.co.bithatch.ninstall.lib.Machine;
import uk.co.bithatch.ninstall.lib.ProgressOutputStream;
import uk.co.bithatch.ninstall.lib.steps.AbstractURLDownload;
import uk.co.bithatch.ninstall.lib.updater.UpdateStep;
import uk.co.bithatch.ninstall.lib.updater.UpdateStepContext;
import uk.co.bithatch.ninstall.lib.updater.UpdaterAttribute;

public class DownloadLatestVersion extends AbstractURLDownload<UpdateStepContext> implements UpdateStep {

    private Path tmpFile;

    @Override
    public void apply(UpdateStepContext context) throws Exception {
        var attrs = context.setup().attributes();
        var downloadUri = (String)attrs.get(UpdaterAttribute.DOWNLOAD_URI);
        if(downloadUri == null)
            throw new IllegalStateException(MessageFormat.format("Download URI is not set. This suggests that {0} did not execute before this step ({1}). Check the configuration of the updater.", CheckLatestVersion.class.getName(), DownloadLatestVersion.class.getName() ));
        
        var url = new URL(Machine.hostMachine().repository(context.setup().setupApp().url()), downloadUri);
        var tmp = Files.createTempFile("ninstall", "update");
        download(context, url, in -> {
            try(var out = Files.newOutputStream(tmp)) {
                in.transferTo(new ProgressOutputStream(out, context.progress()));
            }
            catch(IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        });
    }

    @Override
    public void rollback(UpdateStepContext context) throws Exception {
        Files.deleteIfExists(tmpFile);
    }

}
