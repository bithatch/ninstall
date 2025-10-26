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

public interface Step<CTX extends StepContext<?>> {
    
    /**
     * Initalise the step. Setup any claims on the root progress here. If you do not,
     * the whole step will be assumed to take exactly 1/n, where n is the the total number
     * of steps in the whole setup.
     * <p>
     * For steps that take a dis-proportionately longer (or shorter) time can return a
     * a weighting. For example, a downloaded file is likely to longer than a step that installs 
     * a local file. This can be a positive number, either be > 1.0 for steps that take longer 
     * than usual, or < 1.0. 
     * 
     * @param context
     * @return weighting
     * @throws Exception 
     */
    default float init(CTX context) throws Exception {
        return 1;
    }

    void apply(CTX context) throws Exception;

    void rollback(CTX context) throws Exception;

    default void commit(CTX context)  throws Exception {
    }
}
