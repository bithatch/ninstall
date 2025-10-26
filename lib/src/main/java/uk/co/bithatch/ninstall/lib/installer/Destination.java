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

import java.nio.file.Path;
import java.util.Optional;
import java.util.ResourceBundle;

import uk.co.bithatch.ninstall.lib.SetupAppToolkit;
import uk.co.bithatch.ninstall.lib.SetupPage;

public final class Destination extends SetupPage {
    
    public final static ResourceBundle RESOURCES = ResourceBundle.getBundle(Destination.class.getName());

    public interface DestinationUI<TK extends InstallerToolkit> extends InstallUserInterface<Destination, TK> {
        public final static String DEFAULT_TITLE = RESOURCES.getString("title");
        public final static String DEFAULT_DESCRIPTION = RESOURCES.getString("description");
    }

    public final static class Builder extends SetupPage.Builder<Destination, Builder> {

        private Optional<Path> path = Optional.empty();

        @Override
        public Destination build() {
            return new Destination(this);
        }
    }

    private final Optional<Path> path;

    private Destination(Builder bldr) {
        super(bldr);
        this.path = bldr.path;
    }

    public Optional<Path> path() {
        return path;
    }

    @Override
    public Class<?> uiClass(SetupAppToolkit<?> tk) {
        return DestinationUI.class;
    }

}
