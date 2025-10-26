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
import java.nio.file.Path;
import java.util.Comparator;
import java.util.ResourceBundle;

import uk.co.bithatch.ninstall.lib.IO;
import uk.co.bithatch.ninstall.lib.Stash;
import uk.co.bithatch.ninstall.lib.steps.AbstractRemoveDirectory;
import uk.co.bithatch.ninstall.lib.uninstaller.UninstallStepContext;

public final class RemoveProgramDirectory extends AbstractRemoveDirectory<UninstallStepContext> {

	public final static ResourceBundle RESOURCES = ResourceBundle.getBundle(RemoveProgramDirectory.class.getName());
	
    public RemoveProgramDirectory() {
        super(false, false, false);
    }

    @Override
    protected final Path getPath(UninstallStepContext context) {
        return context.setup().installLocation();
    }

    @Override
    protected final void beforeRemove(UninstallStepContext context, Stash stash) {
        var installloc = context.setup().installLocation();
		var appProperties = installloc.resolve(".app.properties");
        if(Files.exists(appProperties)) {
            stash.stash(appProperties);
        }
        
        IO.ioRun(() -> {
        	try (var walk = Files.walk(installloc)) {
                walk.sorted(Comparator.reverseOrder()).filter(p-> Files.isDirectory(p)).forEach(p -> {
                	try {
                		Files.delete(p);
                		context.progress().info(RESOURCES.getString("removedEmptyDirectory"), p.getFileName());
                	}
                	catch(Exception e) {
                	}
                });
            }

            /* If all that's left is journal files, then stash those */
            var left = Files.walk(installloc).filter(p -> !p.equals(installloc)).count();
            var leftJnls = Files.walk(installloc).filter(p -> p.getFileName().toString().endsWith(".jnl")).count();
            if(left == leftJnls) {
                Files.walk(installloc).filter(p -> p.getFileName().toString().endsWith(".jnl")).forEach(stash::stash);
            }
        });

    }

}
