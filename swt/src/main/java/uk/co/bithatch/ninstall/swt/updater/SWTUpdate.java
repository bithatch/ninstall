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
package uk.co.bithatch.ninstall.swt.updater;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;

import uk.co.bithatch.ninstall.lib.Journals;
import uk.co.bithatch.ninstall.lib.Progress;
import uk.co.bithatch.ninstall.lib.SetupSequence;
import uk.co.bithatch.ninstall.lib.Step;
import uk.co.bithatch.ninstall.lib.updater.Update;
import uk.co.bithatch.ninstall.lib.updater.UpdateContext;
import uk.co.bithatch.ninstall.lib.updater.UpdateStepContext;
import uk.co.bithatch.ninstall.lib.updater.Update.UpdateUI;
import uk.co.bithatch.ninstall.swt.SWTSetupProgress;

public class SWTUpdate extends AbstractSWTUpdateUI<Update> implements UpdateUI<SWTUpdaterToolkit> {

    public final static ResourceBundle RESOURCES = ResourceBundle.getBundle(SWTUpdate.class.getName());
    
    private ProgressBar progress;
    private Label info;
    private boolean complete;
    private Label label;
    private SetupSequence<UpdateStepContext> seq;

	private ProgressBar stepProgress;

	private Label stepInfo;

    public SWTUpdate() {
        super(DEFAULT_TITLE, DEFAULT_DESCRIPTION);
    }

    @Override
    protected void onShow() {
        if (seq == null) {
            new Thread(this::update, "Update").start();
        }
    }

    @Override
    public Visibility nextVisibility() {
        if(isComplete()) {
            return Visibility.VISIBLE;
        }
        else {
            return Visibility.DISABLED;
        }
    }

    protected boolean isComplete() {
        return seq != null && complete;
    }

    @Override
    public Visibility previousVisibility() {
        return nextVisibility();
    }

    @Override
    public boolean canClose() {
        if(isComplete()) {
            return super.canClose();
        }
        else {
            var messageBox = new MessageBox(toolkit().shell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
            messageBox.setText(RESOURCES.getString("interrupt.title"));
            messageBox.setMessage(RESOURCES.getString("interrupt.message"));
            messageBox.setButtonLabels(Map.of(SWT.YES, RESOURCES.getString("interrupt.interrupt")));
            if(messageBox.open() == SWT.YES) {
                if(seq != null)
                    seq.interrupt();
            }
            return false;
        }
    }

    @Override
    protected void onInit(Composite parent) {

        var rowLayout = new GridLayout();
        rowLayout.numColumns = 1;
        parent.setLayout(rowLayout);

        label = new Label(parent, SWT.NONE);
        label.setText(RESOURCES.getString("updating"));
        label.setLayoutData(new GridData(GridData.FILL, 0, true, false));

        progress = new ProgressBar(parent, SWT.SMOOTH);
        progress.setLayoutData(new GridData(GridData.FILL, 0, true, false));
        progress.setMaximum(Integer.MAX_VALUE);

        info = new Label(parent, SWT.NONE);
        info.setText("");
        info.setLayoutData(new GridData(GridData.FILL, 0, true, false));

        stepProgress = new ProgressBar(parent, SWT.SMOOTH);
        stepProgress.setLayoutData(new GridData(GridData.FILL, 0, true, false));
        stepProgress.setMaximum(Integer.MAX_VALUE);

        stepInfo = new Label(parent, SWT.NONE);
        stepInfo.setText("");
        stepInfo.setLayoutData(new GridData(GridData.FILL, 0, true, false));

    }

    private void update() {
        try {
            List<Step<UpdateStepContext>> steps = model().steps();

            var prg = new SWTSetupProgress(toolkit().display(), progress, info);

            seq = new SetupSequence<UpdateStepContext>(prg, () -> {

                var sprg = prg.childProgress(stepProgress, stepInfo);
            	return new UpdateStepContext() {

                    @Override
                    public UpdateContext setup() {
                        return toolkit().setupAppContext();
                    }

    				@Override
    				public Progress progress() {
    					return sprg;
    				}

					@Override
					public Journals journals() {
						return toolkit().journals();
					}
                };
            }, toolkit());
            seq.run(steps);
        } finally {
            complete = true;
            toolkit().display().asyncExec(() -> {
                toolkit().next();
            });
        }
    }
}
