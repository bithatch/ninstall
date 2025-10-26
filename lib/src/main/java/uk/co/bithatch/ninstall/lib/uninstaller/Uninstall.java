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
package uk.co.bithatch.ninstall.lib.uninstaller;

import java.util.ResourceBundle;

import uk.co.bithatch.ninstall.lib.Setup;

public final class Uninstall extends Setup<UninstallStep> {

    public final static ResourceBundle RESOURCES = ResourceBundle.getBundle(Uninstall.class.getName());

    public interface UninstallUI<TK extends UninstallerToolkit> extends UninstallUserInterface<Uninstall, TK> {
        public final static String DEFAULT_TITLE = RESOURCES.getString("title");
        public final static String DEFAULT_DESCRIPTION = RESOURCES.getString("description");
    }

    public final static class Builder extends Setup.Builder<UninstallStep, Uninstall, Builder> {

        @Override
        public Uninstall build() {
            return new Uninstall(this);
        }
    }

    private Uninstall(Builder bldr) {
        super(bldr, UninstallUI.class);
    }

}
