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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Text;

import uk.co.bithatch.ninstall.lib.installer.Destination;
import uk.co.bithatch.ninstall.lib.installer.InstallerAttribute;
import uk.co.bithatch.ninstall.lib.installer.Destination.DestinationUI;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SWTDestination extends AbstractSWTInstallUI<Destination> implements DestinationUI<SWTInstallerToolkit> {

    private Text destination;

    public SWTDestination() {
        super(DEFAULT_TITLE, DEFAULT_DESCRIPTION);
    }

    @Override
    protected void onShow() {

    }

    @Override
    public void onNext() {
        toolkit().setupAppContext().attributes().put(InstallerAttribute.INSTALL_LOCATION, Paths.get(destination.getText()));
    }

    @Override
    protected void onInit(Composite parent) {

        var rowLayout = new GridLayout();
        rowLayout.numColumns = 2;
        parent.setLayout(rowLayout);

        var defaultPath = (Path)toolkit().setupAppContext().attributes().get(InstallerAttribute.INSTALL_LOCATION);

        destination = new Text(parent, SWT.SINGLE | SWT.BORDER);
        destination.setLayoutData(new GridData(GridData.FILL, 0, true, false));
        destination.setText(defaultPath.toString());

        var browse = new Button(parent, SWT.FLAT);
        browse.setText("Browse");
        browse.addSelectionListener(SelectionListener.widgetSelectedAdapter(evt -> {
            var dialog = new DirectoryDialog(parent.getShell(), SWT.SAVE);
            dialog.setMessage("Select Installation Directory");
            dialog.setFilterPath(destination.getText());
            var res = dialog.open();
            if (res != null) {
                destination.setText(res);
            }
        }));

    }

}
