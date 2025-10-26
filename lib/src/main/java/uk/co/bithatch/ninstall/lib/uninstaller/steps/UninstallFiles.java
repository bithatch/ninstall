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
import java.util.Comparator;
import java.util.ResourceBundle;

import uk.co.bithatch.ninstall.lib.IO;
import uk.co.bithatch.ninstall.lib.Journal;
import uk.co.bithatch.ninstall.lib.installer.steps.InstallFiles;
import uk.co.bithatch.ninstall.lib.uninstaller.UninstallStep;
import uk.co.bithatch.ninstall.lib.uninstaller.UninstallStepContext;

public class UninstallFiles implements UninstallStep {
    public final static ResourceBundle RESOURCES = ResourceBundle.getBundle(UninstallFiles.class.getName());
	private static final String UNINSTALL = "uninstall";

    public final static class Builder {
        private boolean removeAll;
        
        public Builder withRemoveAll() {
            return withRemoveAll(true);
        }
        public Builder withRemoveAll(boolean removeAll) {
            this.removeAll =removeAll;
            return this;
        }
    
        public UninstallFiles build() {
            return new UninstallFiles(this);
        }
        
    }
    
    private final boolean removeAll;
    
    private UninstallFiles(Builder bldr) {
        this.removeAll = bldr.removeAll;
    }

    @Override
    public void apply(UninstallStepContext context) throws Exception {
    	var progress = context.progress();
    	context.journals().journalled(UNINSTALL, (stash, journal) -> {
    		
    		var fileJournal = context.journals().obtainJournal(InstallFiles.FILES_JOURNAL_AND_STASH);
			fileJournal.paths().forEach(path -> {
	            var rpath = context.setup().installLocation().resolve(path);
	            stash.stash(rpath);
	            IO.ioRun(() -> Files.deleteIfExists(rpath));
	            progress.info(RESOURCES.getString("uninstalledFile"), path.getFileName().toString());    			
    		});
			fileJournal.delete();
    		
    		/* Remaining journals */
    		for(var otherJournal : Files.walk(context.setup().installLocation()).filter(p -> p.getFileName().toString().startsWith(".") && p.getFileName().toString().endsWith(".jnl")).map(context.journals()::obtainJournal).toList()) {
    			
    			if(otherJournal.equals(journal)) {
    				continue;
    			}
    			
    			otherJournal.paths().forEach(path -> {
		            var rpath = context.setup().installLocation().resolve(path);
		            stash.stash(rpath);
		            IO.ioRun(() -> Files.deleteIfExists(rpath));
		            progress.info(RESOURCES.getString("uninstalledFile"), path.getFileName().toString());
		        });
    		}
	        
	        if(removeAll) {
	            IO.ioRun(() -> {
	                try (var walk = Files.walk(context.setup().installLocation())) {
	                    walk.sorted(Comparator.reverseOrder()).forEach(f -> {
	                        stash.stash(f);
	                        IO.ioRun(() -> Files.deleteIfExists(f));
	                        progress.info(RESOURCES.getString("uninstalledFile"), f.getFileName().toString());
	                    });
	                }
	            });
	        }
        });
    }

    @Override
    public float init(UninstallStepContext context) throws Exception {
        // TODO now wrong
    	return context.journals().journalledCall(UNINSTALL, (stash, journal) -> 
    		(float)journal.size()
    	);
    }

    @Override
    public void rollback(UninstallStepContext context) throws Exception {
    	context.journals().journalled(UNINSTALL, (stash, journal) -> {
    		stash.restoreAndClose(context.progress());
    	});
    }

    @Override
    public void commit(UninstallStepContext context) throws Exception {
    	context.journals().journalled(UNINSTALL, (stash, journal) -> {
			stash.close();
			journal.delete();
    	});
    }

}
