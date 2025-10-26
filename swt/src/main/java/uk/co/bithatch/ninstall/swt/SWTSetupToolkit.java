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

import com.sshtools.liftlib.OS;
import com.sshtools.liftlib.OS.Desktop;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import static uk.co.bithatch.ninstall.swt.SWTLayout.noMargin;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import uk.co.bithatch.ninstall.lib.AbstractSetupToolkit;
import uk.co.bithatch.ninstall.lib.DisplayMode;
import uk.co.bithatch.ninstall.lib.InstallResult;
import uk.co.bithatch.ninstall.lib.SetupAppContext;
import uk.co.bithatch.ninstall.lib.SetupAppOptions;
import uk.co.bithatch.ninstall.lib.SetupPage;
import uk.co.bithatch.ninstall.swt.SWTSetupUI.Visibility;

public abstract class SWTSetupToolkit<
    CTX extends SetupAppContext<?, ?>>
    extends AbstractSetupToolkit<CTX>  {

    private Composite contentPanel;
    private StackLayout stackLayout;
    private final AtomicInteger index = new AtomicInteger(0);
    private Button previousButton;
    private Button nextButton;
    private SWTSetupAppOptions options;
    private Shell shell;
    private CTX setupAppContext;
    private Optional<InstallResult> result = Optional.empty();
    
    protected SWTSetupToolkit() {
        super(DisplayMode.GRAPHICAL);
    }

    @Override
    public SWTSetupAppOptions defaultOptions() {
        return SWTSetupAppOptions.defaultOptions();
    }
    
    public final Display display() {
        return contentPanel.getDisplay();
    }

    @Override
    public final void exit() {
        shell().dispose();
    }

    @Override
    public final boolean isBest() {
        return OS.getDesktopEnvironment() != Desktop.CONSOLE;
    }

    @Override
    public final boolean isValid() {
        return OS.getDesktopEnvironment() != Desktop.CONSOLE;
    }

    @Override
    public final void next() {
        nextPage(setupAppContext.options(), shell());
    }
    
    public final SWTSetupAppOptions options() {
        return options;
    }

    public final void setAvailable() {
        var ui = getCurrentUI();
        configureControl(previousButton, index.get() == 0 ? Visibility.HIDDEN : ui.previousVisibility());
        configureControl(nextButton, ui.nextVisibility());
        nextButton.setText(index.get() < setupAppContext.setupApp().pages().size() - 1 ? "Next" : "Finish");
    }

    @Override
    public final InstallResult setup(CTX setupAppContext) {
        this.setupAppContext = setupAppContext;
        
        this.options = (SWTSetupAppOptions) setupAppContext.options();

        var display = new Display();

        var layout = noMargin(new GridLayout());

        shell = new Shell(display, SWT.SHELL_TRIM);
        shell.setImages(options.icons().stream().map((res) -> SWTResource.resourceImage(display, res)).toList()
                .toArray(new Image[0]));
        shell.setLayout(layout);
        shell.setSize(options.width(), options.height());
        shell.setText(name(setupAppContext));
        
        var bounds = shell.getMonitor().getBounds();
        shell.setLocation(bounds.x + ( (bounds.width - shell.getSize().x) / 2), bounds.y + ( (bounds.height - shell.getSize().y) / 2));
        
        shell.addListener(SWT.Close, l -> {
            var ui = getCurrentUI();
            if(!ui.canClose()) {
                l.doit = false;
            }
        });
        
        var gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessVerticalSpace = true;
        contentPanel = new Composite(shell, SWT.NONE);
        contentPanel.setLayoutData(gridData);
        stackLayout = new StackLayout();
        contentPanel.setLayout(stackLayout);

        var buttonPanel = new Composite(shell, SWT.NONE);
        var buttonLayout = new RowLayout(SWT.HORIZONTAL);
        buttonLayout.fill = true;
        buttonPanel.setLayout(buttonLayout);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.END;
        gridData.grabExcessHorizontalSpace = true;
        buttonPanel.setLayoutData(gridData);

        previousButton = new Button(buttonPanel, SWT.FLAT);
        previousButton.setText("Previous");
        previousButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(evt -> {
            index.decrementAndGet();
            setupPage(options);
        }));

        nextButton = new Button(buttonPanel, SWT.FLAT);
        nextButton.setText("Next");
        nextButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(evt -> {
            getCurrentUI().onNext();
            nextPage(options, shell);
        }));

        setupPage(options);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
        
        return result.orElse(InstallResult.CANCELLED);
    }

    @Override
    public final void result(InstallResult result) {
        this.result = Optional.of(result);
    }
    
    @Override
	public Optional<InstallResult> result() {
		return result;
	}

	@Override
    public final CTX setupAppContext() {
        return setupAppContext;
    }

    public final Shell shell() {
        return shell;
    }

    public final void show(Composite parent) {
        stackLayout.topControl = parent;
        contentPanel.layout();
    }
    
    public final Composite stack() {
        return contentPanel;
    }

    protected abstract String name(CTX installerContext);

    protected void nextPage(SetupAppOptions options, Shell shell) {
        index.incrementAndGet();
        if (index.get() == setupAppContext.setupApp().pages().size()) {
            shell.dispose();
        } else {
            setupPage(options);
        }
    }

    protected abstract SWTSetupUI<SetupPage, SWTSetupToolkit<CTX>> pageUI(SetupPage page);

    private void configureControl(Control ctrl, Visibility vis) {
        switch(vis) {
        case VISIBLE:
            ctrl.setEnabled(true);;
            ctrl.setVisible(true);
            break;
        case HIDDEN:
            ctrl.setVisible(false);
            break;
        default:
            ctrl.setEnabled(false);
            ctrl.setVisible(true);
            break;
        }
    }

    private SWTSetupUI<?, ?> getCurrentUI() {
        return pageUI(setupAppContext.setupApp().pages().get(index.get()));
    }

    private void setupPage(SetupAppOptions options) {
        var pages = setupAppContext.setupApp().pages();
        while(index.get() < pages.size()) {
            var page = pages.get(index.get());
            if(!page.valid(setupAppContext)) {
                if(index.incrementAndGet() == pages.size()) {
                    shell.dispose();
                    return;
                }
            }
            else {
                var ui = pageUI(page);
                ui.show();
                setAvailable();
                break;
            }
        }
    }
}