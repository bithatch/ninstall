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

import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Consumer;

public abstract class AbstractSetupToolkit<
    CTX extends SetupAppContext<?, ?>>
    implements SetupAppToolkit<CTX> {

    private final List<SetupUserInterface<?, ?>> uis = new ArrayList<>();
    private final Set<SetupUserInterface<?, ?>> initialized = new HashSet<>();
    private final DisplayMode mode;
    private Journals journals;
    
    private boolean loaded;
    
    protected AbstractSetupToolkit(DisplayMode mode) {
        this.mode = mode;
        journals = new Journals(this);
    }
    @Override
    public Journals journals() {
    	return journals;
    }
    
    @Override
    public final DisplayMode mode() {
        return mode;
    }
    
    public final <SETUP extends SetupUserInterface<?, ?>, UI> UI ui(Class<SETUP> uiType, Class<UI> uiClass) {
        return ui(uiType, uiClass, null);
    }
    
    @SuppressWarnings("unchecked")
    public final <SETUP extends SetupUserInterface<?, ?>, UI> UI ui(Class<SETUP> uiType, Class<UI> uiClass, Consumer<UI> initializer) {
        if(!loaded) {
            loaded = true;
            for(SETUP ui : ServiceLoader.load(uiType)) {
                uis.add(ui);
            }
        }
        for(var m : uis) {
            if(uiClass.isAssignableFrom(m.getClass())) {
                if(!initialized.contains(m)) {
                    if(initializer == null)
                        throw new IllegalStateException("Request for UI instance for a page that is not initialised, but there is no initializer");
                    initializer.accept((UI)m);
                    
                    initialized.add(m);
                }
                return (UI)m;
            }
        }
        throw new IllegalArgumentException(MessageFormat.format("No implementation for ''{0}'' in ''{1}''", uiClass.getName(), uiType.getName()));
    }
}
