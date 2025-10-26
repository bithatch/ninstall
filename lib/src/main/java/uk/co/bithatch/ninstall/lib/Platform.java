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
package uk.co.bithatch.ninstall.lib;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ServiceLoader;

import uk.co.bithatch.ninstall.lib.installer.InstallStepContext;

public interface Platform {
    
    public final static class Default {
        private final static Platform DEFAULT = ServiceLoader.load(Platform.class).findFirst().orElseThrow(() -> new IllegalStateException("No platform libraries on the classpath. Are you missing ninstall-linux, ninstall-macos or ninstall-windows."));
        
    }
    
    public static Platform get() {
        return Default.DEFAULT;
    }

    void createShortcut(InstallStepContext context, Shortcut shortcut) throws IOException;

    Path resolveShortcut(InstallStepContext context, Shortcut shortcut) throws IOException;

    void removeShortcut(Path path);

    void createService(InstallStepContext context, Service shortcut) throws IOException;

    Path resolveService(InstallStepContext context, Service shortcut) throws IOException;

    void removeService(Path path);
}
