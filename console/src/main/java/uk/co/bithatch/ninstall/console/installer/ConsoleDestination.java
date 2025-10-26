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

import org.jline.builtins.Completers;
import org.jline.keymap.KeyMap;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Reference;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;

import uk.co.bithatch.ninstall.lib.installer.Destination;
import uk.co.bithatch.ninstall.lib.installer.InstallerAttribute;
import uk.co.bithatch.ninstall.lib.installer.Destination.DestinationUI;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ConsoleDestination extends AbstractConsoleInstallUI<Destination> implements DestinationUI<ConsoleInstallerToolkit> {

    public ConsoleDestination() {
        super(DEFAULT_TITLE, DEFAULT_DESCRIPTION);
    }

    @Override
    protected void onShow(Terminal terminal) {

        var parser = new DefaultParser();

        var reader = LineReaderBuilder.builder().
                terminal(terminal).
                completer(new Completers.FileNameCompleter()).
                parser(parser).
                option(LineReader.Option.DISABLE_EVENT_EXPANSION, true).
                variable(LineReader.LIST_MAX, 50).
                build();

        var keyMap = reader.getKeyMaps().get("main");
        keyMap.bind(new Reference("tailtip-toggle"), KeyMap.alt("s"));

        var defaultPath = (Path)toolkit().setupAppContext().attributes().get(InstallerAttribute.INSTALL_LOCATION);
        var pathStr = reader.readLine("> ", null, defaultPath.toString());
        var path = pathStr.equals("") ? defaultPath : Paths.get(pathStr);

        toolkit().setupAppContext().attributes().put(InstallerAttribute.INSTALL_LOCATION, path);
    }

}
