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

import com.sshtools.liftlib.OS;

import uk.co.bithatch.ninstall.lib.InputFileset.FilesetCondition;
import uk.co.bithatch.ninstall.lib.SDKMan.Platform;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;

public record Machine(OperatingSystem os, InstructionSet arch) implements FilesetCondition {
	
	public final static Machine LINUX_AMD64 = new Machine(Systems.LINUX, Arch.AMD64);
	public final static Machine LINUX_AARCH64 = new Machine(Systems.LINUX, Arch.AARCH64);
	public final static Machine WINDOWS_AMD64 = new Machine(Systems.WINDOWS, Arch.AMD64);
	public final static Machine MACOS_AMD64 = new Machine(Systems.MACOS, Arch.AMD64);
	public final static Machine MACOS_AARCH64 = new Machine(Systems.MACOS, Arch.AARCH64);

    public static Machine hostMachine() {
        if(OS.isLinux()) {
            return new Machine(Systems.LINUX, Arch.hostArch());
        }
        else if(OS.isWindows()) {
            return new Machine(Systems.WINDOWS, Arch.hostArch());
        }
        else if(OS.isMacOs()) {
            return new Machine(Systems.MACOS, Arch.hostArch());
        }
        else
            throw new UnsupportedOperationException("OS / Arch combination unknown.");
    }

    public Platform sdkmanPlatform() {
    	if(os.equals(Systems.LINUX)) {
    		if(arch.equals(Arch.X86)) {
    			return Platform.LINUX32;
    		}
    		else if(arch.equals(Arch.AMD64)) {
    			return Platform.LINUX64;
    		}
    		else if(arch.equals(Arch.AARCH64)) {
    			return Platform.LINUXARM64;
    		}
    		else {
    			throw new UnsupportedOperationException("Unknown platform. " + this);
    		}
    	}
    	else if(os.equals(Systems.WINDOWS)) {
    		if(arch.equals(Arch.AMD64)) {
    			return Platform.WINDOWSX64;
    		}
    		else {
    			throw new UnsupportedOperationException("Unknown platform. " + this);
    		}
    	}
    	else {
			throw new UnsupportedOperationException("Unknown platform. " + this);
    	}
    }

    public URL repository(URL url) {
        try {
            return new URL(url, os().name().toLowerCase() + "-" + arch().name().toLowerCase());
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

	@Override
	public boolean evaluate(InputFileset fileset) {
		return hostMachine().equals(this);
	}
}
