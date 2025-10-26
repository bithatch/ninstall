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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class Setup<STEP extends Step<?>> extends SetupPage {

    @SuppressWarnings("unchecked")
    public abstract static class Builder<STEP extends Step<?>, PAGE extends Setup<STEP>, BLDR extends Builder<STEP, PAGE, BLDR>> extends SetupPage.Builder<PAGE, BLDR> {

        private final List<Step<?>> steps = new ArrayList<>();

        public final BLDR withStep(Step<?>... pages) {
            return withStep(Arrays.asList(pages));
        }

        public final BLDR withStep(Collection<Step<?>> steps) {
            this.steps.addAll(steps);
            return (BLDR)this;
        }

    }

    private final List<Step<?>> steps;
    private final Class<?> uiClass;

    protected Setup(Builder<STEP,?,?> bldr, Class<?> uiClass) {
        super(bldr, Mode.values());
        this.uiClass = uiClass;
        this.steps = Collections.unmodifiableList(new ArrayList<>(bldr.steps));
    }

    @Override
    public final Class<?> uiClass(SetupAppToolkit tk) {
        return uiClass;
    }
    
    @SuppressWarnings("unchecked")
    public final <STEPS extends List<? extends Step<?>>> STEPS steps() {
        return (STEPS)steps;
    }
}
