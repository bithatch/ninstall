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
package uk.co.bithatch.ninstall.console;

import java.text.MessageFormat;
import java.util.Optional;

import me.tongfei.progressbar.ProgressBar;
import uk.co.bithatch.ninstall.lib.Progress;

public final class ConsoleProgress implements Progress {

    private static final int BAR_RANGE = 1000;
    
	private final Progress defPrg;
	private final ProgressBar pb;
	private final ConsoleProgress parent;
	private float weight = 1; 
    private double current;
    private double total;
	
    private float childProgress;    

	public ConsoleProgress(ProgressBar pb) {
		this(pb, null);
	}

	public ConsoleProgress(ProgressBar pb, ConsoleProgress parent) {
		this.defPrg = Progress.defaultProgress();
		this.pb = pb;
		this.parent = parent;
		pb.maxHint(BAR_RANGE);
	}

	@Override
	public void alert(String text, Object... args) {
		if(parent == null)
			defPrg.alert(text, args);
		else 
			parent.alert(text, args);
	}

	@Override
	public void command(String text, Object... args) {
		if(current == 0) {
			if(parent == null)
				defPrg.command(text, args);
			else 
				parent.command(text, args);
		}
	}

	@Override
	public void error(String text, Throwable exception, Object... args) {
		if(parent == null)
			defPrg.error(text, exception, args);
		else
			parent.error(text, exception, args);
	}

	@Override
	public void info(String text, Object... args) {
//		if(parent == null && current == 0)
//			defPrg.info(text, args);
		
		if (args.length == 0)
			pb.setExtraMessage(text);
		else
			pb.setExtraMessage(MessageFormat.format(text, args));
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
        updateBar();
	}

	@Override
	public void reset() {
		this.total = current = 0;
		updateBar();
	}

	@Override
	public double total() {
		return total;
	}

	@Override
	public void total(double total) {
		this.total = total;
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
		this.weight = weight;
	}

	private void updateChild(ConsoleProgress child) {
		childProgress = Math.min(0.999f, child.fraction()) * child.weight();
		updateBar();
	}

	private float fraction() {
		return (float)((current +  childProgress ) / Math.max(1, total)); 
	}

	private void updateBar() {
    	if(parent != null) {
    		parent.updateChild(this);
    	}
    	pb.stepTo( (int)(fraction() * 1000.0));
	}
}