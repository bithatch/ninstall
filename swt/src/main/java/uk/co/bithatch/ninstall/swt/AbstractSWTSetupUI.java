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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import uk.co.bithatch.ninstall.lib.Journals;
import uk.co.bithatch.ninstall.lib.SetupApp;
import uk.co.bithatch.ninstall.lib.SetupPage;


public abstract class AbstractSWTSetupUI<
    TK extends SWTSetupToolkit<?>, 
    MODEL extends SetupPage> 
    implements SWTSetupUI<MODEL, TK> {

    protected MODEL model;
    
    private final String defaultTitle;
    private final String defaultDescription;
    private Composite parent;
    private Composite content;
    private TK toolkit;
    
    protected AbstractSWTSetupUI(String defaultTitle, String defaultDescription) {
        this.defaultTitle = defaultTitle;
        this.defaultDescription = defaultDescription;
    }

    @Override
    public final void init(MODEL model, TK toolkit) {
        this.toolkit = toolkit;
        this.model = model;
        
        var display = toolkit.display();
        var app = toolkit.setupAppContext().setupApp();

        // create the second page's content
        parent = new Composite(toolkit.stack(), SWT.NONE);
        
        var rowLayout = new GridLayout();
        parent.setLayout(rowLayout);
        
        header(model, toolkit, display, app);

        content = new Composite(parent, SWT.NONE);

        var gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessVerticalSpace = true;
        content.setLayoutData(gridData);
        
        onInit(content);
    }

    protected void header(MODEL model, TK toolkit, Display display, SetupApp<?, ?> app) {
        toolkit.options().banner().ifPresent(banner -> {
            var ctrl = banner.create(parent);
            var gd = new GridData();
            gd.heightHint = banner.height();
            gd.horizontalAlignment = GridData.FILL;
            gd.grabExcessHorizontalSpace = true;
            ctrl.setLayoutData(gd);
        });
        
        var titleLabel = new Label(parent, SWT.NONE);
        titleLabel.setFont(new Font(display, new FontData(display.getSystemFont().getFontData()[0].getName(), 22, SWT.NORMAL)));
        titleLabel.setText(app.processString(model.title().orElse(defaultTitle)));
        
        new Label (parent, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(GridData.FILL, 0, true, false));
        
        var descriptionLabel = new Label(parent, SWT.WRAP);
        descriptionLabel.setText(app.processString(model.description().orElse(defaultDescription)));
        descriptionLabel.setLayoutData(new GridData(SWT.FILL, 0, true, false));
    }
    
    protected abstract void onInit(Composite content);

    @Override
    public final void show() {
        toolkit.show(parent);

//      layout.topControl = pageNum == 0 ? page0 : page1;
//      contentPanel.layout();
        
        onShow();
    }

    protected abstract void onShow();

    @Override
    public final TK toolkit() {
        return toolkit;
    }

    public final MODEL model() {
        return model;
    }

    protected final String titleText() {
        return model.title().orElse(defaultTitle);
    }

}
