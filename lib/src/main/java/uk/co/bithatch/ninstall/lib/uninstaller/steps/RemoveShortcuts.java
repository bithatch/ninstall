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
package uk.co.bithatch.ninstall.lib.uninstaller.steps;

import java.nio.file.Files;
import java.util.ResourceBundle;

import uk.co.bithatch.ninstall.lib.Platform;
import uk.co.bithatch.ninstall.lib.installer.steps.CreateShortcuts;
import uk.co.bithatch.ninstall.lib.uninstaller.UninstallStep;
import uk.co.bithatch.ninstall.lib.uninstaller.UninstallStepContext;

public class RemoveShortcuts implements UninstallStep {
	public final static ResourceBundle RESOURCES = ResourceBundle.getBundle(RemoveShortcuts.class.getName());

	@Override
	public void apply(UninstallStepContext context) throws Exception {
		context.journals().journalled(CreateShortcuts.SHORTCUTS, (stash, journal) -> {

			var platform = Platform.get();
			var progress = context.progress();

			progress.adjustTotal(journal.size());
			journal.paths().forEach(path -> {
				var rpath = context.setup().installLocation().resolve(path);
				if (Files.exists(rpath))
					stash.stash(rpath);
				else
					context.progress().alert(RESOURCES.getString("shortcutMissing"), rpath.getFileName());
				platform.removeShortcut(rpath);
				progress.info(RESOURCES.getString("shortcutRemoved"), path.getFileName());
				progress.step();
			});
			journal.delete();
		});
	}

	@Override
	public void rollback(UninstallStepContext context) throws Exception {
		context.journals().journalled(CreateShortcuts.SHORTCUTS, (stash, journal) -> {
			stash.restoreAndClose(context.progress());
		});
	}

	@Override
	public void commit(UninstallStepContext context) throws Exception {
		context.journals().journalled(CreateShortcuts.SHORTCUTS, (stash, journal) -> {
			stash.close();
			journal.delete();
		});
	}

}
