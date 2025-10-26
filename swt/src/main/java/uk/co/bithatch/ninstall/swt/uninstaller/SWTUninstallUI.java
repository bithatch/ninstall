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
package uk.co.bithatch.ninstall.swt.uninstaller;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import java.util.Map;
import java.util.ResourceBundle;

import uk.co.bithatch.ninstall.lib.SetupPage;
import uk.co.bithatch.ninstall.lib.uninstaller.UninstallUserInterface;
import uk.co.bithatch.ninstall.swt.SWTSetupUI;

public interface SWTUninstallUI<PAGE extends SetupPage> extends SWTSetupUI<PAGE, SWTUninstallerToolkit>, UninstallUserInterface<PAGE, SWTUninstallerToolkit>  {

    public final static ResourceBundle RESOURCES = ResourceBundle.getBundle(SWTUninstallUI.class.getName());

    @Override
    default boolean canClose() {
        var messageBox = new MessageBox(toolkit().shell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        messageBox.setText(RESOURCES.getString("close"));
        messageBox.setMessage(RESOURCES.getString("message"));
        messageBox.setButtonLabels(Map.of(SWT.YES, "Close"));
        return messageBox.open() == SWT.YES;
    }
    
}
