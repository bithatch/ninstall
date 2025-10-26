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
package uk.co.bithatch.ninstall.swt;

import java.text.MessageFormat;
import java.util.Optional;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

import uk.co.bithatch.ninstall.lib.Progress;

public final class SWTSetupProgress implements Progress {
    private static final int BAR_RANGE = 1000;
	private final Progress defPrg;
    private final Display display;
    private final ProgressBar bar;
    private final Label text;
    private final SWTSetupProgress parent;
    
    private float childProgress;    
    private double current;
    private double total;
	private float weight = 1;

    public SWTSetupProgress(Display display, ProgressBar bar, Label text) {
    	this(display, bar, text, null);
    }
    
    private SWTSetupProgress(Display display, ProgressBar bar, Label text, SWTSetupProgress parent) {
        this.defPrg = Progress.defaultProgress();
        this.parent = parent;
        this.display = display;
        this.bar = bar;
        this.text = text;
        display.asyncExec(() -> bar.setMaximum(BAR_RANGE));
    }
    
    @Override
    public void alert(String text, Object... args) {
        defPrg.alert(text, args);
    }

    public SWTSetupProgress childProgress(ProgressBar bar, Label text) {
    	return new SWTSetupProgress(display, bar, text, this);
    }

    @Override
    public void command(String text, Object... args) {
        defPrg.command(text, args);
    }

    @Override
    public void error(String text, Throwable exception, Object... args) {
        defPrg.error(text, exception, args);
    }

    private float fraction() {
		return (float)((current +  childProgress ) / Math.max(1, total)); 
	}

    @Override
    public void info(String text, Object... args) {
        defPrg.info(text, args);
        display.asyncExec(() -> {
            if (args.length == 0)
            	this.text.setText(text);
            else
            	this.text.setText(MessageFormat.format(text, args));
        });
    }

    @Override
	public Progress parent() {
		return Optional.ofNullable(parent).orElseThrow(() -> new IllegalStateException("No parent."));
	}

    @Override
	public double progressed() {
		return current;
	}

    @Override
    public void progressed(double amount) {
        current = Math.min(total, amount);
        display.asyncExec(this::updateBar);
    }

	@Override
	public void reset() {
		this.total = current = 0;
        display.asyncExec(this::updateBar);
	}

	@Override
	public double total() {
		return total;
	}

	@Override
    public void total(double total) {
        this.total = total;
        display.asyncExec(this::updateBar);
    }
	
	private void updateBar() {
    	if(parent != null) {
    		parent.updateChild(this);
    	}
    	bar.setSelection( (int)(fraction() * 1000.0));
	}

	private void updateChild(SWTSetupProgress child) {
		childProgress = Math.min(0.999f, child.fraction()) * child.weight();
		updateBar();
	}

	@Override
    public void warning(String text, Object... args) {
        defPrg.warning(text, args);
    }

	@Override
	public float weight() {
		return weight;
	}

	@Override
	public void weight(float weight) {
		this.weight  = weight;
	}
}