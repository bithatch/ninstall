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
package uk.co.bithatch.ninstall.swt.updater;

import uk.co.bithatch.ninstall.lib.SetupPage;
import uk.co.bithatch.ninstall.lib.updater.UpdateContext;
import uk.co.bithatch.ninstall.lib.updater.UpdaterToolkit;
import uk.co.bithatch.ninstall.swt.SWTSetupToolkit;
import uk.co.bithatch.ninstall.swt.SWTSetupUI;

public class SWTUpdaterToolkit extends SWTSetupToolkit<UpdateContext> implements UpdaterToolkit {

    @Override
    protected String name(UpdateContext installerContext) {
        return installerContext.setupApp().name();
    }

    @Override
    protected SWTSetupUI<SetupPage, SWTSetupToolkit<UpdateContext>> pageUI(SetupPage page) {
        @SuppressWarnings("unchecked")
        var clazz = (Class<SWTSetupUI<SetupPage, SWTSetupToolkit<UpdateContext>>>)page.uiClass(this);
        return ui(SWTUpdateUI.class, clazz, u -> {
            u.init(page, this); 
        });
    }
}