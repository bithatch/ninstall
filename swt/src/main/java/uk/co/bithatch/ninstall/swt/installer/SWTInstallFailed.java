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
package uk.co.bithatch.ninstall.swt.installer;

import java.util.ResourceBundle;


import org.eclipse.swt.widgets.Composite;

import uk.co.bithatch.ninstall.lib.Lang;
import uk.co.bithatch.ninstall.lib.SetupAttribute;
import uk.co.bithatch.ninstall.lib.installer.InstallFailed;
import uk.co.bithatch.ninstall.lib.installer.InstallFailed.FailedUI;
import uk.co.bithatch.ninstall.swt.SWTFailure;

public class SWTInstallFailed extends AbstractSWTInstallUI<InstallFailed>
		implements FailedUI<SWTInstallerToolkit> {

	public final static ResourceBundle RESOURCES = ResourceBundle.getBundle(SWTInstallFailed.class.getName());

	public SWTInstallFailed() {
		super(DEFAULT_TITLE, DEFAULT_DESCRIPTION);
	}

	@Override
	public Visibility previousVisibility() {
		return Visibility.HIDDEN;
	}

	@Override
	public boolean canClose() {
		return true;
	}

	@Override
	protected void onShow() {

	}

	@Override
	protected void onInit(Composite parent) {
		new SWTFailure(parent, Lang
				.unwrapException(toolkit().setupAppContext().get(SetupAttribute.SETUP_ERROR, Exception.class).get()), RESOURCES);
	}

}
