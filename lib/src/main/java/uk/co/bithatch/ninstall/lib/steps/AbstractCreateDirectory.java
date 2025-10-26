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
package uk.co.bithatch.ninstall.lib.steps;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.text.MessageFormat;

import uk.co.bithatch.ninstall.lib.ElevatableStep;
import uk.co.bithatch.ninstall.lib.ElevatedHelpers;
import uk.co.bithatch.ninstall.lib.StepContext;

public abstract class AbstractCreateDirectory<CTX extends StepContext<?>> implements ElevatableStep<CTX> {
    
    private String[] created;

    @Override
	public final void apply(CTX context) throws Exception {
    	 var path = getPath(context);
         var progress = context.progress();
         if(context.elevator().closure(new ElevatedHelpers.FileExists(path))) {
             if(context.elevator().closure(new ElevatedHelpers.IsDirectory(path))) {
                 progress.info(MessageFormat.format("{0} already exists", path));
             }
             else {
                 throw new FileAlreadyExistsException(path.toString());
             }
         }
         else {
         	 progress.info(MessageFormat.format("Creating {0}", path));
         	 created = context.elevator().closure(new ElevatedHelpers.CreateDirectory(path));
             progress.info(MessageFormat.format("Created {0}", path));
         }
	}

	@Override
	public final void rollback(CTX context) throws Exception {
        if(created != null) {
            var path = getPath(context);
            try {
            	context.progress().info(MessageFormat.format("Deleting {0}", path));
            	context.elevator().closure(new ElevatedHelpers.DeleteFile(path));
            }
            finally {
                created = null;
            }
        }
	}

    protected abstract Path getPath(CTX context);
}
