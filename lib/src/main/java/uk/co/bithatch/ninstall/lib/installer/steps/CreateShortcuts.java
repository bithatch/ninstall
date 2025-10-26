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

import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.ResourceBundle;

import uk.co.bithatch.ninstall.lib.Platform;
import uk.co.bithatch.ninstall.lib.Shortcut;
import uk.co.bithatch.ninstall.lib.installer.InstallStep;
import uk.co.bithatch.ninstall.lib.installer.InstallStepContext;

public class CreateShortcuts implements InstallStep {
	public static final String SHORTCUTS = "shortcuts";

	public final static ResourceBundle RESOURCES = ResourceBundle.getBundle(CreateShortcuts.class.getName());

	private final Collection<Shortcut> shortcuts;

	public CreateShortcuts(Shortcut... launchers) {
		this(Arrays.asList(launchers));
	}

	public CreateShortcuts(Collection<Shortcut> launchers) {
		this.shortcuts = launchers;
	}

	@Override
	public void apply(InstallStepContext context) throws Exception {
		var progress = context.progress();
		progress.adjustTotal(shortcuts.size());
		context.journals().journalled(SHORTCUTS, (stash, journal) -> {
			var platform = Platform.get();
			for (var shortcut : shortcuts) {
				var path = platform.resolveShortcut(context, shortcut);
				if (Files.exists(path)) {
					stash.stash(path);
				}
				platform.createShortcut(context, shortcut);
				journal.log(path);
				progress.info(RESOURCES.getString("shortcutInstalled"), shortcut.defaultDisplayName());
				progress.step();
			}
		});
	}

	@Override
	public void rollback(InstallStepContext context) throws Exception {
		context.journals().journalled(SHORTCUTS, (stash, journal) -> {
			stash.restoreAndClose(context.progress());
		});
	}

	@Override
	public void commit(InstallStepContext context) throws Exception {
		context.journals().journalled(SHORTCUTS, (stash, journal) -> {
			stash.close();
		});
	}

}
