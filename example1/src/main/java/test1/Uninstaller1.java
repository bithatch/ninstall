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
package test1;

import uk.co.bithatch.ninstall.lib.SetupAppFactory;
import uk.co.bithatch.ninstall.lib.steps.RemoveDirectory;
import uk.co.bithatch.ninstall.lib.uninstaller.Uninstall;
import uk.co.bithatch.ninstall.lib.uninstaller.UninstallFinished;
import uk.co.bithatch.ninstall.lib.uninstaller.UninstallWelcome;
import uk.co.bithatch.ninstall.lib.uninstaller.Uninstaller;
import uk.co.bithatch.ninstall.lib.uninstaller.steps.DeregisterApplication;
import uk.co.bithatch.ninstall.lib.uninstaller.steps.RemoveProgramDirectory;
import uk.co.bithatch.ninstall.lib.uninstaller.steps.RemoveShortcuts;
import uk.co.bithatch.ninstall.lib.uninstaller.steps.UninstallFiles;

public class Uninstaller1  implements SetupAppFactory<Uninstaller> {
	
    public static void main(String[] args) {
        new Uninstaller1().app().setup(args);
    }

	@Override
	public Uninstaller app() {
		return new Uninstaller.Builder().
	            withHumanReadableAppId("LogonBox VPN Client").
	            withOptions(Test1.graphicalOptions()).
	            withPage(
	                new UninstallWelcome.Builder().
	                    build(),
	                uninstall(),
	                new UninstallFinished.Builder().build()
	            ).
	            build();
	}

    static Uninstall uninstall() {
        return new Uninstall.Builder().
            withStep(
                new RemoveShortcuts(),
                new UninstallFiles.Builder().build(),
                new RemoveDirectory.Builder().
                    withPath("logs").
                    withRecursive().
                    build(),
                new RemoveProgramDirectory(),
                new DeregisterApplication()
            ).
            build();
    }
}
