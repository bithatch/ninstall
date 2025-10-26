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
import uk.co.bithatch.ninstall.lib.installer.Agreement;
import uk.co.bithatch.ninstall.lib.installer.Destination;
import uk.co.bithatch.ninstall.lib.installer.Install;
import uk.co.bithatch.ninstall.lib.installer.InstallFinished;
import uk.co.bithatch.ninstall.lib.installer.InstallWelcome;
import uk.co.bithatch.ninstall.lib.installer.Installer;
import uk.co.bithatch.ninstall.lib.installer.steps.CreateProgramDirectory;
import uk.co.bithatch.ninstall.lib.installer.steps.CreateShortcuts;
import uk.co.bithatch.ninstall.lib.installer.steps.InstallFiles;
import uk.co.bithatch.ninstall.lib.installer.steps.InstallSDK;
import uk.co.bithatch.ninstall.lib.installer.steps.RegisterApplication;
import uk.co.bithatch.ninstall.lib.installer.steps.UninstallPrevious;
import uk.co.bithatch.ninstall.lib.steps.CreateDirectory;

public class Installer1 implements SetupAppFactory<Installer> {
	
    @Override
	public Installer app() {
		return new Installer.Builder("LogonBox VPN").
            withHumanReadableAppId("LogonBox VPN Client").
            withOptions(Test1.graphicalOptions()).
            withPage(
                new InstallWelcome.Builder().
                    build(),
                new Agreement.Builder().
                    withContent(Test1.class.getResource("/licenses/APACHE-2.txt")).
                    build(),
                new Destination.Builder().
                    build(),
                new Install.Builder().
                    withStep(
                        new UninstallPrevious(Uninstaller1.uninstall()),
                        new RegisterApplication(),
                        new CreateProgramDirectory(),
                        new CreateDirectory("logs"),
                        InstallSDK.java(),
                        new InstallFiles(),
                        new CreateShortcuts(Test1.shortcuts())
                    ).
                    build(),
                new InstallFinished.Builder().build()
            ).
            build();
	}

	public static void main(String[] args) {
        new Installer1().app().setup(args);
    }
}
