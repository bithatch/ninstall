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

import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import uk.co.bithatch.ninstall.lib.Step;
import uk.co.bithatch.ninstall.lib.StepContext;

public abstract class AbstractURLDownload<CTX extends StepContext<?>> implements Step<CTX> {

    public final static ResourceBundle RESOURCES = ResourceBundle.getBundle(AbstractURLDownload.class.getName());
    
    protected void download(CTX context, URL url, Consumer<InputStream> inConsumer) throws Exception {
    	var progress = context.progress();
        progress.info(MessageFormat.format(RESOURCES.getString("opening"), url));
        
        var urlConnection = url.openConnection();
        var len = urlConnection.getContentLengthLong();
        if(len > -1) {
        	progress.adjustTotal(len);
        }

        progress.info(MessageFormat.format(RESOURCES.getString("downloading"), url));
        try(var in = urlConnection.getInputStream()) {
            inConsumer.accept(in);
//            try(var out = Files.newOutputStream(destination)) {
//                in.transferTo(new ProgressOutputStream(out, context));
//            }
        }
        progress.info(MessageFormat.format(RESOURCES.getString("downloaded"), url));
        
        
    }
}
