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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class SWTFailure {

	public SWTFailure(Composite parent, Exception exception, ResourceBundle RESOURCES) {
		var rowLayout = new GridLayout();
		rowLayout.numColumns = 1;
		parent.setLayout(rowLayout);

		var label = new Label(parent, SWT.WRAP);
		label.setText(exception.getLocalizedMessage() == null ? RESOURCES.getString("noMessage")
				: exception.getLocalizedMessage());
		var lgd = new GridData(GridData.FILL, 0, true, false);
		lgd.verticalIndent = 50;
		label.setLayoutData(lgd);

		var fontData = label.getFont().getFontData()[0];
		var font = new Font(parent.getDisplay(), new FontData(fontData.getName(), fontData.getHeight(), SWT.BOLD));
		label.setFont(font);

		var copy = new Button(parent, SWT.PUSH);
		copy.setText(RESOURCES.getString("copy"));
		var gd = new GridData(0, 0, true, false);
		gd.verticalIndent = 50;
		copy.setLayoutData(gd);
		copy.addSelectionListener(SelectionListener.widgetSelectedAdapter(c -> {
			var clipboard = new Clipboard(parent.getDisplay());
			var sw = new StringWriter();
			exception.printStackTrace(new PrintWriter(sw));
			clipboard.setContents(new Object[] { sw.toString() }, new Transfer[] { TextTransfer.getInstance() });
			clipboard.dispose();
		}));

	}
}
