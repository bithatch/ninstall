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
package test1;

import static uk.co.bithatch.ninstall.lib.Resource.ofNamed;
import static uk.co.bithatch.ninstall.lib.Resource.ofResource;

import java.util.Arrays;
import java.util.List;

import uk.co.bithatch.ninstall.lib.InputFileset;
import uk.co.bithatch.ninstall.lib.Locations;
import uk.co.bithatch.ninstall.lib.Manifest;
import uk.co.bithatch.ninstall.lib.OutputFileset;
import uk.co.bithatch.ninstall.lib.Resource;
import uk.co.bithatch.ninstall.lib.Shortcut;
import uk.co.bithatch.ninstall.lib.packaging.SelfExtractingExecutablePackager;
import uk.co.bithatch.ninstall.swt.SWTBanner;
import uk.co.bithatch.ninstall.swt.SWTSetupAppOptions;
import uk.co.bithatch.ninstall.swt.SWTBanner.Pos;

public class Test1 {

    static SWTSetupAppOptions graphicalOptions() {
        return new SWTSetupAppOptions.Builder().
            withIcons(
            	Resource.ofResources(Test1.class, 
                "/install-logo256px.png",
                "/install-logo128px.png",
                "/install-logo64px.png",
                "/install-logo48px.png",
                "/install-logo32px.png",
                "/install-logo24px.png",
                "/install-logo16px.png"
               )
            ).
            withBanner(new SWTBanner.Builder().
                    withBackgroundColor("#1E0C51").
                    withImagePosition(Pos.L).
                    withImageResource(
                    	Resource.ofResource("banner.png")
                    ).
                    build()).
            build();
    }
    
    static List<Shortcut> shortcuts() {
        return Arrays.asList(
            new Shortcut.Builder("logonbox-vpn-cli").
                withIcons(
                    ofNamed("terminal")
                ).
                withTerminal().
                build(),
            new Shortcut.Builder("logonbox-vpn-tray").
                withAutostart().
                withIcons(
                    ofResource(Test1.class, "/logo256px.png"),
                    ofResource(Test1.class, "/logo128px.png"),
                    ofResource(Test1.class, "/logo64px.png"),
                    ofResource(Test1.class, "/logo48px.png"),
                    ofResource(Test1.class, "/logo32px.png"),
                    ofResource(Test1.class, "/logo24px.png"),
                    ofResource(Test1.class, "/logo16px.png")
                ).
                build()
        );
    }
    
//    static List<Shortcut> services() {
//        return Arrays.asList(
//            new Shortcut.Builder("logonbox-vpn-cli").
//                build(),
//            new Shortcut.Builder("logonbox-vpn-dbus-daemon").
//                withType(Type.SERVICE).
//                build(),
//            new Shortcut.Builder("logonbox-vpn-dbus-desktop-service").
//                withType(Type.SERVICE).
//                build(),
//            new Shortcut.Builder("logonbox-vpn-tray").
//                withType(Type.SERVICE).
//                build()
//        );
//    }

    public static void main(String[] args) {
        var rootPath = "/home/tanktarta/Documents/Git/logonbox-desktop-vpn-clients"; 
        
        var mf = new Manifest.Builder("logonbox-vpn", "4.0.0-SNAPSHOT").
            withOutput(new OutputFileset.Builder(Locations.PROGRAMS).
            	withInputBase(rootPath).
                withInput(
                	InputFileset.include("cli/target/logonbox-vpn-cli"),
                    InputFileset.include("dbus-daemon/target/logonbox-vpn-dbus-daemon"),
                    InputFileset.include("desktop-service/target/logonbox-vpn-service"),
                    InputFileset.include("tray/target/logonbox-vpn-tray")
                ).
                build()).
            build();
        
//        var pkgr = new DirectoryPackager.Builder().
//        		withPreInstall(new PreInstall()).
//                withInstaller(VPNInstaller.class).
//                withVerboseOutput(true).
//        		build();
        
        var pkgr = new SelfExtractingExecutablePackager.Builder().
                withPreInstall(new PreInstall()).
                withInstaller(new Installer1()).
                withUninstaller(new Uninstaller1()).
                withUpdater(new Updater1()).
                withVerboseOutput(true).
                build();
        
        var pkg = pkgr.make(mf);
        
        System.out.println(pkg.location());
        
    }
    
    public final static class PreInstall implements Runnable {

        @Override
        public void run() {
        }
        
        public static void main(String[] args) {
        }
    }
}
