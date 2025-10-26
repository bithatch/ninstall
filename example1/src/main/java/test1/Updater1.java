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

import uk.co.bithatch.ninstall.lib.Resource;
import uk.co.bithatch.ninstall.lib.SetupAppFactory;
import uk.co.bithatch.ninstall.lib.updater.Update;
import uk.co.bithatch.ninstall.lib.updater.UpdateFinished;
import uk.co.bithatch.ninstall.lib.updater.UpdateWelcome;
import uk.co.bithatch.ninstall.lib.updater.Updater;
import uk.co.bithatch.ninstall.lib.updater.steps.CheckLatestVersion;
import uk.co.bithatch.ninstall.swt.SWTSetupAppOptions;

public class Updater1 implements SetupAppFactory<Updater> {
    public static void main(String[] args) {
        new Updater1().app().setup(args);
    }

	@Override
	public Updater app() {
		return  new Updater.Builder().
            withHumanReadableAppId("LogonBox VPN Client").
            withOptions(new SWTSetupAppOptions.Builder().
                withHeight(192).
                withIcons(Resource.ofResources(Updater1.class,
                        "install-logo256px.png",
                        "install-logo128px.png",
                        "install-logo64px.png",
                        "install-logo48px.png",
                        "install-logo32px.png",
                        "install-logo24px.png",
                        "install-logo16px.png"
                        )
                    ).
                    build()).
            withPage(
                new UpdateWelcome.Builder().build(),
                new Update.Builder().
                    withStep(
                        new CheckLatestVersion()
                    ).
                    build(),
                new UpdateFinished.Builder().build()
            ).
            build();
	}
}
