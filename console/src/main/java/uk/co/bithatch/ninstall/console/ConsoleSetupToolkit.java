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

import com.sshtools.liftlib.OS;
import com.sshtools.liftlib.OS.Desktop;

import uk.co.bithatch.ninstall.lib.AbstractSetupToolkit;
import uk.co.bithatch.ninstall.lib.DisplayMode;
import uk.co.bithatch.ninstall.lib.InstallResult;
import uk.co.bithatch.ninstall.lib.SetupAppContext;
import uk.co.bithatch.ninstall.lib.SetupAppOptions;
import uk.co.bithatch.ninstall.lib.SetupPage;

import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ConsoleSetupToolkit<
    CTX extends SetupAppContext<?, ?>>
    extends AbstractSetupToolkit<CTX>  {

    private Terminal terminal;
    private final AtomicInteger index = new AtomicInteger(0);
    private CTX setupAppContext;
    private Optional<InstallResult> result;

    protected ConsoleSetupToolkit() {
        super(DisplayMode.CONSOLE);
        try {
          terminal = TerminalBuilder.builder().build();
          } catch (IOException e) {
              throw new UncheckedIOException(e);
          }
    }

    public final String separator() {
        var ln = new StringBuilder();
        var width = terminal.getWidth() == 0 ? 80 : terminal.getWidth();
        for(int i = 0 ; i < width ; i++)
            ln.append('-');
        return ln.toString();
    }
    
    public final Optional<Boolean> yesNoDefault() {
        var agree = new AttributedStringBuilder();
        // Agree
        agree.style(AttributedStyle.DEFAULT.bold());
        agree.append("(");
        agree.append("Y");
        agree.append(")es, I accept the agreement and wish to continue");
        agree.style(AttributedStyle.DEFAULT.boldOff());
        agree.append(System.lineSeparator());
        agree.append("(");
        agree.style(AttributedStyle.DEFAULT.bold());
        agree.append("N");
        agree.style(AttributedStyle.DEFAULT.boldOff());
        agree.append(")o, I do not accept and wish to exit");
        agree.append(System.lineSeparator());
        agree.append("or just RETURN to review the text again");
        agree.append(System.lineSeparator());
        agree.append(System.lineSeparator());
        agree.print(terminal);
        terminal.flush();
        
        var lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
        
        var answer = lineReader.readLine("> ").toLowerCase();
        if(answer.equals("y") || answer.equals("yes")) {
            return Optional.of(true);
        }
        else if(answer.equals("n") || answer.equals("no")) {
            return Optional.of(false);
        }
        return Optional.empty();
    }
    
    public final Terminal terminal() {
        return terminal;
    }

    @Override
    public final boolean isBest() {
        return OS.getDesktopEnvironment() == Desktop.CONSOLE;
    }

    @Override
    public final InstallResult setup(CTX setupAppContexct) {
        this.setupAppContext = setupAppContexct;
        var setupApp = setupAppContexct.setupApp();
        
        var pages = setupApp.pages();
        int thisIndex;
        while((thisIndex = index.get()) < pages.size()) {
            SetupPage page = pages.get(thisIndex);
            if(!page.valid(setupAppContexct)) {
                index.incrementAndGet();
                continue;
            }
            ConsoleSetupUI<SetupPage, ConsoleSetupToolkit<CTX>> ui = pageUI(page);
            ui.show();
            
            var newIndex = index.get();
            if(newIndex == thisIndex)
                index.set(thisIndex + 1);
        }
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

	protected abstract ConsoleSetupUI<SetupPage, ConsoleSetupToolkit<CTX>> pageUI(SetupPage page);

    protected abstract String name(CTX installerContext);

    @Override
    public CTX setupAppContext() {
        return setupAppContext;
    }

    @Override
    public void exit() {
        index.set(Integer.MAX_VALUE);
    }

    @Override
    public void next() {
        index.addAndGet(1);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public SetupAppOptions defaultOptions() {
        return ConsoleSetupAppOptions.defaultOptions();
    }
}
