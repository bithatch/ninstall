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
package uk.co.bithatch.ninstall.console.uninstaller;

import java.util.List;
import java.util.ResourceBundle;

import org.jline.terminal.Terminal;

import uk.co.bithatch.ninstall.console.ConsoleProgress;
import uk.co.bithatch.ninstall.lib.Journals;
import uk.co.bithatch.ninstall.lib.Progress;
import uk.co.bithatch.ninstall.lib.SetupSequence;
import uk.co.bithatch.ninstall.lib.Step;
import uk.co.bithatch.ninstall.lib.uninstaller.Uninstall;
import uk.co.bithatch.ninstall.lib.uninstaller.UninstallStepContext;
import uk.co.bithatch.ninstall.lib.uninstaller.UninstallationContext;
import uk.co.bithatch.ninstall.lib.uninstaller.Uninstall.UninstallUI;

public class ConsoleUninstall extends AbstractConsoleUninstallUI<Uninstall>
		implements UninstallUI<ConsoleUninstallerToolkit> {

	public final static ResourceBundle RESOURCES = ResourceBundle.getBundle(ConsoleUninstall.class.getName());

	public ConsoleUninstall() {
		super(DEFAULT_TITLE, DEFAULT_DESCRIPTION);
	}

	@Override
	protected void onShow(Terminal terminal) {
		var setupAppContext = toolkit().setupAppContext();
		List<Step<UninstallStepContext>> steps = model().steps();

		try (var pb1 = bar1("uninstalling", setupAppContext).build(); var pb2 = bar2(setupAppContext).build()) {

			var prg = new ConsoleProgress(pb1);

			new SetupSequence<UninstallStepContext>(prg, () -> {
				return new UninstallStepContext() {

					private ConsoleProgress prg2 = new ConsoleProgress(pb2, prg);

					@Override
					public UninstallationContext setup() {
						return setupAppContext;
					}

					@Override
					public Progress progress() {
						return prg2;
					}

					@Override
					public Journals journals() {
						return toolkit.journals();
					}
				};
			}, toolkit).run(steps);
		}
	}

}
