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

import static uk.co.bithatch.ninstall.lib.IO.ioRun;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

import uk.co.bithatch.ninstall.lib.Machine;
import uk.co.bithatch.ninstall.lib.steps.AbstractURLDownload;
import uk.co.bithatch.ninstall.lib.updater.UpdateStep;
import uk.co.bithatch.ninstall.lib.updater.UpdateStepContext;
import uk.co.bithatch.ninstall.lib.updater.UpdaterAttribute;

public class CheckLatestVersion extends AbstractURLDownload<UpdateStepContext> implements UpdateStep {

    private Path tmpFile;

    @Override
    public void apply(UpdateStepContext context) throws Exception {
        var meta = new URL(Machine.hostMachine().repository(context.setup().setupApp().url()), "meta.properties");
        var props = new Properties();
        download(context, meta, (in) -> {
            ioRun(() ->  props.load(in));
        });
        var attrs = context.setup().attributes();
        attrs.put(UpdaterAttribute.AVAILABLE_VERSION, Objects.requireNonNull(props.getProperty("version")));
        attrs.put(UpdaterAttribute.DOWNLOAD_URI, Objects.requireNonNull(props.getProperty("uri")));
        attrs.put(UpdaterAttribute.DOWNLOAD_LEN, Long.parseLong(Objects.requireNonNull(props.getProperty("length"))));
    }

    @Override
    public void rollback(UpdateStepContext context) throws Exception {
        Files.deleteIfExists(tmpFile);
    }

}
