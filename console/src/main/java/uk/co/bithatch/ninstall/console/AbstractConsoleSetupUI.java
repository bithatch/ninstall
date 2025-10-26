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

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import uk.co.bithatch.ninstall.lib.Journals;
import uk.co.bithatch.ninstall.lib.SetupAppContext;
import uk.co.bithatch.ninstall.lib.SetupPage;


public abstract class AbstractConsoleSetupUI<
    TK extends ConsoleSetupToolkit<?>, 
    MODEL extends SetupPage> 
    implements ConsoleSetupUI<MODEL, TK> {

    protected TK toolkit;
    protected MODEL model;
    
    private final String defaultTitle;
    private final String defaultDescription;
    
    protected AbstractConsoleSetupUI(String defaultTitle, String defaultDescription) {
        this.defaultTitle = defaultTitle;
        this.defaultDescription = defaultDescription;
    }

    @Override
    public final void init(MODEL model, TK toolkit) {
        this.toolkit = toolkit;
        this.model = model;
    }

    @Override
    public final void show() {
        var terminal = toolkit.terminal();

        terminal.puts(InfoCmp.Capability.clear_screen);

        printHeader(terminal);
        
        onShow(terminal);
    }

    @Override
    public final TK toolkit() {
        return toolkit;
    }

    public final MODEL model() {
        return model;
    }

	protected ProgressBarBuilder bar1(String taskNameKey, SetupAppContext<?, ?> setupAppContext) {
		return new ProgressBarBuilder().
                setTaskName(MessageFormat.format(taskNameKey, setupAppContext.setupApp().name())).
                setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR);
	}

	protected ProgressBarBuilder bar2(SetupAppContext<?, ?> setupAppContext) {
		return new ProgressBarBuilder().
                showSpeed().
                setTaskName("").
                setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR);
	}

    protected void printHeader(Terminal terminal) {
        var app = toolkit.setupAppContext().setupApp();
        
        var text = new AttributedStringBuilder();
        text.append(toolkit.separator());
        text.append(System.lineSeparator());
        
        text.style(AttributedStyle.DEFAULT.bold());
        text.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        text.append(app.processString(titleText()));
        text.style(AttributedStyle.DEFAULT.foregroundDefault());
        text.style(AttributedStyle.DEFAULT.boldOff());
        text.append(System.lineSeparator());
        text.append(System.lineSeparator());

        // Description
        text.append(app.processString(model.description().orElse(defaultDescription)));
        text.append(System.lineSeparator());
        text.append(toolkit.separator());
        text.append(System.lineSeparator());

        
        // Print
        text.print(terminal);
        terminal.flush();
    }

    protected final String titleText() {
        return model.title().orElse(defaultTitle);
    }

    protected abstract void onShow(Terminal terminal);

}
