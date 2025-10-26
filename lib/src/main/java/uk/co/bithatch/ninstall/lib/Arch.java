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

public enum Arch implements InstructionSet {
    AMD64, AARCH64, X86, ANY;

    public static Arch hostArch() {
        if (isAarch64())
            return Arch.AARCH64;
        else if (is64bit())
            return Arch.AMD64;
        else
            return Arch.X86;
    }

    public static boolean is64bit() {
        return Defaults.IS_64BIT;
    }

    public static boolean isAarch64() {
        return Defaults.IS_AARCH64;
    }
    
    private static class Defaults {

        private static final boolean IS_64BIT = is64bit0();
        private static final boolean IS_AARCH64 = isAarch640();

        private static boolean is64bit0() {
            String systemProp = System.getProperty("com.ibm.vm.bitmode");
            if (systemProp != null) {
                return "64".equals(systemProp);
            }
            systemProp = System.getProperty("sun.arch.data.model");
            if (systemProp != null) {
                return "64".equals(systemProp);
            }
            systemProp = System.getProperty("java.vm.version");
            return systemProp != null && systemProp.contains("_64");
        }

        private static boolean isAarch640() {
            return "aarch64".equals(System.getProperty("os.arch"));
        }
    }
}
