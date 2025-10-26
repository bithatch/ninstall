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
package uk.co.bithatch.ninstall.console.installer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.jline.builtins.Less;
import org.jline.builtins.Source;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

import uk.co.bithatch.ninstall.lib.IO;
import uk.co.bithatch.ninstall.lib.installer.Agreement;
import uk.co.bithatch.ninstall.lib.installer.Agreement.AgreementUI;

public class ConsoleAgreement extends AbstractConsoleInstallUI<Agreement> implements AgreementUI<ConsoleInstallerToolkit> {

    public ConsoleAgreement() {
        super(DEFAULT_TITLE, DEFAULT_DESCRIPTION);
    }
    
    @Override
    protected void onShow(Terminal terminal) {
        
        var lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
        
        while(true) {
            
            lineReader.readLine("Press RETURN > ");
        
            // Content
            if(terminal.getSize().getColumns() > 0) {
	            var less = new Less(terminal, IO.cwd());
	            less.quitAtFirstEof = true;
	            try {
	                less.run(new Source() {
	    
	                    @Override
	                    public String getName() {
	                        return titleText();
	                    }
	    
	                    @Override
	                    public InputStream read() throws IOException {
	                    	return model.resource().in();
	                    }
	    
	                    @Override
	                    public Long lines() {
	                        return null;
	                    }
	                    
	                });
	            }
	            catch(InterruptedException ie) {
	                throw new IllegalStateException("Interrupted.", ie);
	            } catch (IOException e) {
	                throw new UncheckedIOException(e);
	            }
	            terminal.puts(InfoCmp.Capability.clear_screen);
            }
            else {
            	terminal.writer().println(model.resource().asString());
            }
            
            printHeader(terminal);
            
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
            
            var answer = lineReader.readLine("> ").toLowerCase();
            if(answer.equals("y") || answer.equals("yes")) {
                toolkit().next();
                break;
            }
            else if(answer.equals("n") || answer.equals("no")) {
                toolkit().exit();
                return;
            }
        }
    }

}
