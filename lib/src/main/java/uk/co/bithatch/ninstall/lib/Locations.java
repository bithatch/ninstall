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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

public enum Locations implements Where {
    COMMANDS, CONFIGURATION, LIBRARIES, DATA, LOGS, TEMPORARY, STATE, PROGRAMS, DAEMONS, IMAGES, DOCUMENTATION, SDKS;

    @Override
    public Path resolve(Machine target, Layout layout) {
        switch (layout) {
        case FLAT:
            switch (this) {
            case PROGRAMS:
                return Path.of("");
            case CONFIGURATION:
                return Paths.get("conf");
            case LIBRARIES:
                return Paths.get("lib");
            case DATA:
                return Paths.get("data");
            case LOGS:
                return Paths.get("logs");
            case DOCUMENTATION:
                return Paths.get("doc");
            case TEMPORARY:
                return Paths.get("tmp");
            case COMMANDS:
                return Paths.get("bin");
            case DAEMONS:
                return Paths.get("daemons");
            case IMAGES:
                return Paths.get("images");
            case SDKS:
                return Paths.get("sdks");
            default:
                throw unsupported(target, layout);
            }
        case OPERATING_SYSTEM:
            return target.os().resolve(target.arch(), this);
        default:
            throw unsupported(target, layout);
        }
    }

    private UnsupportedOperationException unsupported(Machine target, Layout layout) {
        return new UnsupportedOperationException(MessageFormat.format(
                "The location ''{0}'' is not supported on ''{1}'' for a layout of ''{2}''", this, target, layout));
    }
}
