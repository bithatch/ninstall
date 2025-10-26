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

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Paths;

import uk.co.bithatch.ninstall.lib.Where.Layout;

public class JavaLauncher {

	public static final String APP_NAME = "java-launcher";

	public static void main(String[] args) throws Exception {
		
		var launcherExe = ProcessHandle.current().info().command().orElseThrow(() -> new IllegalStateException("Cannot determine current executablke"));
		var launcherPath = Paths.get(launcherExe).normalize();
		
		if(!Files.exists(launcherPath) && launcherPath.getNameCount() == 1) {
			for(var path : System.getenv("PATH").split(File.pathSeparator)) {
				launcherPath = Paths.get(path).resolve(launcherPath);
				if(Files.exists(launcherPath)) {
					break;
				}
			}
		}
		
		var cmdname = IO.stripExtension(launcherPath.toString());
		
		while(Files.isSymbolicLink(launcherPath)) {
			launcherPath = Files.readSymbolicLink(launcherPath);
		}

		if(!Files.exists(launcherPath)) {
			throw new IllegalStateException("Could not find current executable location.");
		}
		
		var installdir = launcherPath.toAbsolutePath().getParent();
		System.out.println(installdir);
		var layout = Files.exists(installdir.resolve(AppAttribute.APP_PROPERTIES)) ? Layout.FLAT : Layout.OPERATING_SYSTEM;
		
		var sdk = SDK.java(installdir);

		System.out.println("Machine: " + Machine.hostMachine());
		System.out.println("Layout: " + layout);
		
		var confdir = installdir.resolve(Locations.CONFIGURATION.resolve(layout));
		System.out.println("Conf dir: " + confdir);
		
		var conffile = confdir.resolve(cmdname + ".argfile");
		System.out.println("Conf file: " + conffile);
		
		var javacmd = sdk.command(layout, "java").toString();
		System.out.println("Java: " + javacmd);
		
		var pb = new ProcessBuilder(javacmd, "@" + conffile.toAbsolutePath());
		pb.redirectError(Redirect.INHERIT);
		pb.redirectInput(Redirect.INHERIT);
		pb.redirectOutput(Redirect.INHERIT);
		
		System.exit(pb.start().waitFor());
		
	}
}
