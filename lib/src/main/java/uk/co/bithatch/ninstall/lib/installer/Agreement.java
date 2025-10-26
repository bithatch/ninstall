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
package uk.co.bithatch.ninstall.lib.installer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ResourceBundle;

import uk.co.bithatch.ninstall.lib.Resource;
import uk.co.bithatch.ninstall.lib.SetupAppToolkit;
import uk.co.bithatch.ninstall.lib.SetupPage;

public final class Agreement extends SetupPage {
    
    public final static ResourceBundle RESOURCES = ResourceBundle.getBundle(Agreement.class.getName());

    public interface AgreementUI<TK extends InstallerToolkit> extends InstallUserInterface<Agreement, TK> { 
        public final static String DEFAULT_TITLE = RESOURCES.getString("title");
        public final static String DEFAULT_DESCRIPTION = RESOURCES.getString("description");
    }

    public final static class Builder extends SetupPage.Builder<Agreement, Builder> {

		private Resource resource;

        public Builder withContent(String content) {
            return withResource(Resource.ofContent(content));
        }

        public Builder withContent(URL content) {
            return withResource(Resource.ofContent(content));
        }

        public Builder withContent(Reader content) {
            try(var wrt = new StringWriter()) {
                content.transferTo(wrt);
                return withContent(wrt.toString());
            }
            catch(IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        }
        
        public Builder withResource(Resource resource) {
        	this.resource = resource;
        	return this;
        }

        @Override
        public Agreement build() {
            return new Agreement(this);
        }
    }

    private final Resource resource;

    private Agreement(Builder bldr) {
        super(bldr);
        if(bldr.resource == null)
            throw new IllegalArgumentException("No resource.");
        this.resource = bldr.resource;
    }

    public Resource resource() {
        return resource;
    }

	@Override
    public Class<?> uiClass(SetupAppToolkit<?> tk) {
        return AgreementUI.class;
    }
    

}
