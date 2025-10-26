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
package uk.co.bithatch.ninstall.swt.installer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.co.bithatch.ninstall.lib.installer.Agreement;
import uk.co.bithatch.ninstall.lib.installer.Agreement.AgreementUI;
import uk.co.bithatch.ninstall.swt.SWTFontUtils;

public class SWTAgreement extends AbstractSWTInstallUI<Agreement> implements AgreementUI<SWTInstallerToolkit> {

    private Button agree;
    private Button disagree;

    public SWTAgreement() {
        super(DEFAULT_TITLE, DEFAULT_DESCRIPTION);
    }

    @Override
    protected void onShow() {
        
    }

    @Override
    protected void onInit(Composite parent) {
        
       var rowLayout = new GridLayout();
       rowLayout.numColumns = 2;
       parent.setLayout(rowLayout);
        
       var styledText = new StyledText(parent, SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
       styledText.setFont(SWTFontUtils.getMonospacedFont());
       var styledTextLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
       styledTextLayoutData.horizontalSpan = 2;
       styledText.setLayoutData(styledTextLayoutData);
       styledText.setText(model().resource().asString());
       
       agree = new Button(parent, SWT.RADIO);
       agree.addSelectionListener(SelectionListener.widgetSelectedAdapter(evt -> setAvailable()));
       var agreeLabel = new Label(parent, SWT.NONE);
       agreeLabel.setText("Yes, I accept the agreement and wish to continue");
       agreeLabel.setLayoutData(new GridData(SWT.FILL, 0, true, false));
       
       disagree = new Button(parent, SWT.RADIO);
       disagree.addSelectionListener(SelectionListener.widgetSelectedAdapter(evt -> setAvailable()));
       disagree.setSelection(true);
       var disagreeLabel = new Label(parent, SWT.NONE);
       disagreeLabel.setText("No, I do not accept and wish to exit");
       disagreeLabel.setLayoutData(new GridData(SWT.FILL, 0, true, false));
       
       agreeLabel.addMouseListener(MouseListener.mouseUpAdapter(e ->  { 
           agree.setSelection(true);  
           disagree.setSelection(false);  
           setAvailable();
        }));
       disagreeLabel.addMouseListener(MouseListener.mouseUpAdapter(e -> { 
           disagree.setSelection(true);  
           agree.setSelection(false);  
           setAvailable();
        }));

    }
    
    @Override
    public Visibility nextVisibility() {
        if(agree.getSelection()) {
            return Visibility.VISIBLE;
        }
        else {
            return Visibility.DISABLED;
        }
    }

    private void setAvailable() {
        toolkit().setAvailable();        
    }

}
