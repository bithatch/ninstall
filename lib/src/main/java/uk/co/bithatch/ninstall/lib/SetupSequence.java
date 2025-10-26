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
package uk.co.bithatch.ninstall.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Supplier;

public final class SetupSequence<STEP extends StepContext<?>> {
    
	private final Supplier<STEP> ctx;
    private final SetupAppToolkit<?> toolkit;
    
    private boolean interrupted;
    private Thread thread;
	private Progress progress;

    public SetupSequence(Progress progress, Supplier<STEP> ctx, SetupAppToolkit<?> toolkit) {
        this.ctx = ctx;
        this.toolkit = toolkit;
        this.progress = progress;
    }
    
    public void interrupt() {
        interrupted = true;
        if(thread != null)
            thread.interrupt();
    }

    public void run(List<Step<STEP>> steps) {
        
        thread = Thread.currentThread();
        try {
            var undoable = new ArrayList<Step<STEP>>();
            var undoErrs = new ArrayList<Throwable>();
            var commitErrs = new ArrayList<Throwable>();
            var ctxs = new HashMap<Step<STEP>, STEP>(); 
            var autostep = new HashSet<Step<STEP>>(); 
            
            try {
	            for (var step : steps) {
	            	var ctx = this.ctx.get();
	            	var totalWas = progress.total();
	            	var weight = step.init(ctx);
	            	if(weight == 0)
	            		weight = 1;
	            	
	            	if(weight != 1) {
	            		/* Step is weighted (has a single operation). */
	            		if(progress.total() != totalWas)
	            			throw new IllegalArgumentException("If you return a weight other than `1`, you should adjust the root progress yourself.");
	            		progress.adjustTotal(weight);
	            		ctx.progress().weight(weight);
	            		autostep.add(step);
	            	}
	            	else if(totalWas == progress.total()) {
	            		/* Step did not increase total itself during init (has a single operation), do it ourselves */
	            		progress.adjustTotal(1);
	            		autostep.add(step);
	            	}
	            	
	            	ctxs.put(step, ctx);
	            }
            }
            catch (Throwable t) {
                if(t.getCause() instanceof InterruptedException) {
                    t = t.getCause();
                }
                toolkit.result(InstallResult.FAILED);
                toolkit.setupAppContext().attributes().put(SetupAttribute.SETUP_ERROR, t);
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                }
                throw new RuntimeException(t);
            } 
            
            for (var step : steps) {
            	var ctx = ctxs.get(step);
                try {
                	progress.info(ResourceBundle.getBundle(step.getClass().getName()).getString("title"));
            		
                    step.apply(ctx);
                    if(interrupted) {
                        throw new InterruptedException();
                    }
                    ctx.progress().complete();
                    
                    undoable.add(0, step);
                    ctx.progress().reset();
                    if(autostep.contains(step)) {
                    	progress.step(ctx.progress().weight());
                    }
                } catch (Throwable t) {
                    if(t.getCause() instanceof InterruptedException) {
                        t = t.getCause();
                    }
                    undoable.forEach(undoStep -> {
                        try {
                            undoStep.rollback(ctx);
                        } catch (Throwable undoT) {
                            undoErrs.add(undoT);
                        }
                    });
                    toolkit.result(InstallResult.FAILED);
                    toolkit.setupAppContext().attributes().put(SetupAttribute.SETUP_ERROR, t);
                    if(!undoErrs.isEmpty()) {
                        progress.error("Installer tried to roll-back, but there were {0} error(s). The installation is likely now damaged.", undoErrs.size());
                    }
                    if (t instanceof RuntimeException) {
                        throw (RuntimeException) t;
                    }
                    throw new RuntimeException(t);
                }
            }
            undoable.forEach(step -> {
                try {
                	var ctx = ctxs.get(step);
                    step.commit(ctx);
                    if(autostep.contains(step))
                    	progress.step(-1);
                } catch (Exception e) {
                	e.printStackTrace();
                    commitErrs.add(e);
                }
            });
            if(!commitErrs.isEmpty()) {
                progress.error("Installer tried to commit, but there were {0} error(s). The installation probably isn't damaged, but there may be temporary files remaining.", commitErrs.size());
            }
            toolkit.result(InstallResult.INSTALLED);
        }
        finally {
        	toolkit.journals().close();
            thread = null;
        }
    }
}
