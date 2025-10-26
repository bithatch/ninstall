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

import java.util.Optional;
import java.util.function.Function;

public final class Branch extends SetupPage {

    public final static class Builder extends SetupPage.Builder<Branch, Builder> {
    	
    	private Optional<SetupPage> onPass = Optional.empty();
    	private Optional<SetupPage> onFail = Optional.empty();
		private final Function<SetupAppToolkit<?>, Boolean> condition;
    	
    	public Builder(Function<SetupAppToolkit<?>, Boolean> condition) {
    		this.condition = condition;
    	}
    	
    	public Builder onPass(SetupPage page) {
    		this.onPass = Optional.of(page);
    		return this;
    	}
    	
    	public Builder onFail(SetupPage page) {
    		this.onFail= Optional.of(page);
    		return this;
    	}

        @Override
        public Branch build() {
            return new Branch(this);
        }
    }
	private Optional<SetupPage> onPass;
	private Optional<SetupPage> onFail;
	private final Function<SetupAppToolkit<?>, Boolean> condition;

    private Branch(Builder bldr) {
        super(bldr);
        this.onFail = bldr.onFail;
        this.onPass = bldr.onPass;
        this.condition = bldr.condition;
    }

    @Override
    public Class<?> uiClass(SetupAppToolkit<?> tk) {
    	if(condition.apply(tk))
    		return onPass.orElseThrow(() -> new IllegalStateException("FilesetCondition passed, but no page to branch to")).uiClass(tk);
    	else
    		return onFail.orElseThrow(() -> new IllegalStateException("FilesetCondition failed, but no page to branch to")).uiClass(tk);
    }

}
