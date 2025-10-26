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
package uk.co.bithatch.ninstall.lib.uninstaller.steps;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;

import uk.co.bithatch.ninstall.lib.uninstaller.UninstallStep;
import uk.co.bithatch.ninstall.lib.uninstaller.UninstallStepContext;

public class DeregisterApplication implements UninstallStep {
    
    private Path registered;

    @Override
    public void apply(UninstallStepContext context) throws Exception {
        var installation = context.setup();
        var registry = installation.registry();
        registered = registry.get(installation.setupApp().appId()).orElseThrow(() -> new IOException(MessageFormat.format("No application with the ID ''{0}'' is registered. Uninstallation cannot complete.", installation.setupApp().appId())));
        registry.deregister(installation.setupApp().appId());
        context.progress().info("De-registered application");
    }

    @Override
    public void rollback(UninstallStepContext context) throws Exception {
        var installation = context.setup();
        installation.registry().register(installation.setupApp().appId(), registered);
        context.progress().info("Re-registered application @ {0}", registered);
    }
}
