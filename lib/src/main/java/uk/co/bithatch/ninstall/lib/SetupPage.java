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

import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

public abstract class SetupPage implements ExecutableBuilderDecorator {

    @SuppressWarnings("unchecked")
    public static abstract class Builder<PAGE extends SetupPage, BLDR extends Builder<PAGE, BLDR>> {
        private Optional<String> title = Optional.empty();
        private Optional<String> description = Optional.empty();
        private Optional<ResourceBundle> bundle = Optional.empty();
        private Optional<Mode[]> modes = Optional.empty();
        
        public BLDR withTitle(String title) {
            this.title = Optional.of(title);
            return (BLDR)this;
        }
        
        public BLDR withDescription(String description) {
            this.description = Optional.of(description);
            return (BLDR)this;
        }
        
        public BLDR withBundle(ResourceBundle bundle) {
            this.bundle = Optional.of(bundle);
            return (BLDR)this;
        }
        
        public BLDR withModes(Mode... modes) {
            this.modes = Optional.of(modes);
            return (BLDR)this;
        }
        
        public abstract PAGE build();
        
    }
    
    private final Optional<String> title;
    private final Optional<String> description;
    private final Optional<ResourceBundle> bundle;
    private final Mode[] modes;
    
    protected SetupPage(Builder<?,?> bldr) {
        this(bldr, Mode.INTERACTIVE);
    }
    
    protected SetupPage(Builder<?,?> bldr, Mode... defaultModes) {
        title = bldr.title;
        description = bldr.description;
        bundle = bldr.bundle;
        modes = bldr.modes.isPresent() ? bldr.modes.get() : defaultModes;
    }

    public abstract Class<?> uiClass(SetupAppToolkit<?> tk);

    public final Optional<String> title() {
        return title;
    }

    public final Optional<String> description() {
        return description;
    }

    public final Optional<ResourceBundle> bundle() {
        return bundle;
    }
    
    public <CTX extends SetupAppContext<?, ?>> boolean valid(CTX setupAppContexct) {
        if(modes.length > 0)
            return Arrays.asList(modes).contains(setupAppContexct.mode());
        else
            return true;
    }

}
