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

import static uk.co.bithatch.ninstall.lib.IO.ioRun;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import uk.co.bithatch.ninstall.lib.IO;
import uk.co.bithatch.ninstall.lib.ProgressOutputStream;
import uk.co.bithatch.ninstall.lib.installer.InstallStep;
import uk.co.bithatch.ninstall.lib.installer.InstallStepContext;

public final class InstallFiles implements InstallStep {

    public static final String FILES_JOURNAL_AND_STASH = "files";

	public final static ResourceBundle RESOURCES = ResourceBundle.getBundle(InstallFiles.class.getName());
    
	private int files;
//
    @Override
    public float init(InstallStepContext context) throws Exception {
    	files = context.setup().archiveAnalysis().entries();
		context.progress().parent().adjustTotal(files);
        return InstallStep.super.init(context);
    }

    @Override
    public void apply(InstallStepContext context) throws Exception {
    	var progress = context.progress();
    	var analysis = context.setup().archiveAnalysis();
    	
    	context.journals().journalled(FILES_JOURNAL_AND_STASH, (stash, journal) -> {
    		
    		var rootPath = analysis.path();
	        var installLocation = context.setup().installLocation();
	        
	        var fc = new AtomicInteger();
	        try (var stream = Files.walk(rootPath, Integer.MAX_VALUE)) {
	            stream.filter(p -> analysis.filter().test(p)).forEach(file -> { 
	            	try {
	            		progress.reset();
	            		
		                var rel = rootPath.relativize(file);                    
		                var install = installLocation.resolve(rel);
		                
		                stash.stashOrCreated(install);
		                
		                if(Files.isDirectory(file)) {
		                	ioRun(() -> {
		                        var attrs = IO.attrs(file);
		                        Files.createDirectories(install);
		                        attrs.set(install);
		                    });
		                }
		                else {
		                	progress.total(Files.size(file));
		                    ioRun(() -> {
		                        var attrs = IO.attrs(file);
		                        try(var iout = Files.newOutputStream(install)) {
		                            try(var in = Files.newInputStream(file)) {
		                                in.transferTo(new ProgressOutputStream(iout, progress));
		                            }
		                        }
		                        attrs.set(install);
		                    });
		                    progress.info(RESOURCES.getString("installed"), rel.toString());
		                }
		                journal.log(rel);
	            	}
	            	catch(IOException ioe) {
	            		throw new UncheckedIOException(ioe);
	            	}
	            	finally {
	            		fc.incrementAndGet();
	            		progress.complete();
	            		progress.parent().step();
	
	                	IO.delay(10);
	            	}
	            });
	        }
    	});
    }

    @Override
    public void rollback(InstallStepContext context) throws Exception {
    	context.journals().journalled(FILES_JOURNAL_AND_STASH, (stash, journal) -> {
    		stash.restoreAndClose(context.progress());
    	});
    }

    @Override
    public void commit(InstallStepContext context) throws Exception {
    	context.journals().journalled(FILES_JOURNAL_AND_STASH, (stash, journal) -> {
    		stash.close();
    	});
    }
}
