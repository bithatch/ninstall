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

import uk.co.bithatch.ninstall.lib.InputFileset.FilesetCondition;

public enum Systems implements OperatingSystem, FilesetCondition {
    LINUX, WINDOWS, MACOS, ANY;

    @Override
    public Path resolve(InstructionSet arch, Where where) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public String executable(String programName) {
        if(this == WINDOWS) {
            return programName  +".exe";
        }
        return programName;
    }

    @Override
    public Path installRoot(Scope scope) {
        switch(this) {
        case LINUX:
            switch(scope) {
            case GLOBAL:
                return Paths.get("/opt");
            case USER:
                return Paths.get(System.getProperty("user.home")).resolve("Applications");
            default:
                throw new UnsupportedOperationException("TODO");
            }
        default:
            throw new UnsupportedOperationException("TODO");
        }
    }
}
