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
package uk.co.bithatch.ninstall.lib.installer.steps;

import java.util.Arrays;
import java.util.Collection;
import java.util.ResourceBundle;

import uk.co.bithatch.ninstall.lib.Launcher;
import uk.co.bithatch.ninstall.lib.installer.InstallStep;
import uk.co.bithatch.ninstall.lib.installer.InstallStepContext;

public class CreateLaunchers implements InstallStep {
	public static final String LAUNCHERS = "launchers";

	public final static ResourceBundle RESOURCES = ResourceBundle.getBundle(CreateLaunchers.class.getName());

	private final Collection<Launcher> launchers;

	public CreateLaunchers(Launcher launchers) {
		this(Arrays.asList(launchers));
	}

	public CreateLaunchers(Collection<Launcher> launchers) {
		this.launchers = launchers;
	}

	@Override
	public void apply(InstallStepContext context) throws Exception {
		var progress = context.progress();
		progress.adjustTotal(launchers.size());
		context.journals().journalled(LAUNCHERS, (stash, journal) -> {
			for (var launcher : launchers) {
				launcher.create(stash, journal, context.setup());
				progress.info(RESOURCES.getString("launcherCreated"), launcher.name());
				progress.step();
			}
		});
	}

	@Override
	public void rollback(InstallStepContext context) throws Exception {
		context.journals().journalled(LAUNCHERS, (stash, journal) -> {
			stash.restoreAndClose(context.progress());
		});
	}

	@Override
	public void commit(InstallStepContext context) throws Exception {
		context.journals().journalled(LAUNCHERS, (stash, journal) -> {
			stash.close();
		});
	}

}
