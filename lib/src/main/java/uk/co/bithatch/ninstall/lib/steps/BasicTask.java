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

import java.util.Optional;

import uk.co.bithatch.ninstall.lib.Step;
import uk.co.bithatch.ninstall.lib.StepContext;

public final class BasicTask<CTX extends StepContext<?>> implements Step<CTX> {
	
	@FunctionalInterface
	public interface Task {
		void task() throws Exception;
	}
    
    private Task apply;
    private Optional<Task> rollback;

    public BasicTask(Task apply) {
    	this.apply = apply;
    	this.rollback = Optional.empty();
    }
    
    public BasicTask(Task apply, Task rollback) {
    	this.apply = apply;
    	this.rollback = Optional.of(rollback);
    }

    @Override
    public void apply(CTX context) throws Exception {
        apply.task(); 
    }

    @Override
    public void rollback(CTX context) throws Exception {
        if(rollback.isPresent())
        	rollback.get().task();
    }
}
