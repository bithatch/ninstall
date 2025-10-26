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
package uk.co.bithatch.ninstall.linux;

import static uk.co.bithatch.ninstall.lib.IO.checkParentDir;
import static uk.co.bithatch.ninstall.lib.IO.ioRun;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import uk.co.bithatch.ninstall.lib.IO;
import uk.co.bithatch.ninstall.lib.Platform;
import uk.co.bithatch.ninstall.lib.Resource;
import uk.co.bithatch.ninstall.lib.Service;
import uk.co.bithatch.ninstall.lib.Shortcut;
import uk.co.bithatch.ninstall.lib.installer.InstallStepContext;

public class LinuxPlatform implements Platform {

    private static final String PLATFORM = "platform";

	@Override
    public void createShortcut(InstallStepContext context, Shortcut shortcut) throws IOException {

        var path = resolveShortcut(context, shortcut);
        checkParentDir(path);
        try (var pw = new PrintWriter(Files.newBufferedWriter(path))) {
            pw.println("#!/usr/bin/env xdg-open");
            pw.println("[Desktop Entry]");
            pw.println("Version=1.0");
            pw.println("Terminal=" + shortcut.terminal());

            var icons = shortcut.icon();
            shortcut.defaultIcon().ifPresent(def -> {
                pw.println("Icon=" + processIcon(context, null, shortcut, def));
                if (icons.size() > 1) {
                    for (var en : icons.entrySet()) {
                        pw.println("Icon[" + en.getKey().toLanguageTag() + "]=" + processIcon(context, en.getKey(), shortcut, en.getValue()));
                    }
                }
            });
            pw.println("Name=" + shortcut.defaultDisplayName());
            for (var en : shortcut.displayName().entrySet()) {
                pw.println("Name[" + en.getKey().toLanguageTag() + "]=" + en.getValue());
            }
            shortcut.defaultDescription().ifPresent(def -> {
                pw.println("Comment=" + def);
                var comments = shortcut.description();
                if (comments.size() > 1) {
                    for (var en : comments.entrySet()) {
                        pw.println("Comment[" + en.getKey().toLanguageTag() + "]=" + en.getValue());
                    }
                }
            });

            var commandPath = context.setup().installLocation().resolve(shortcut.commandPath());
            pw.println("Exec=" + commandPath.toString().replace(" ", "\\ ")
                    + (shortcut.arguments().isEmpty() ? "" : " " + String.join(" ", shortcut.arguments().stream().map(s->s.replace(" ", "\\ ")).toList())));

            if (!shortcut.categories().isEmpty())
                pw.println("Categories=" + String.join(";", shortcut.categories()));

            pw.println("StartupNotify=false"); // TODO never in Java unless native/jna etc

            pw.println("Type=Application");
            if (shortcut.autostart()) {
                pw.println("X-GNOME-Autostart-enabled=true");
            }
            if (!shortcut.categories().isEmpty())
                pw.println("Keywords=" + String.join(";", shortcut.keywords()));
        }

    }

    private String processIcon(InstallStepContext context, Locale locale, Shortcut shortcut, Resource[] def) {
        for(var res : def) {
            switch(res.source()) {
            case NAMED:
                return res.name();
            case INSTALLED:
                return context.setup().installLocation().resolve(res.name()).toAbsolutePath().toString();
            case BUNDLED:
            	return context.journals().uncheckedJournalledCall(PLATFORM, (stash, journal) -> {
            		var installdir = context.setup().installLocation();
					var dir = installdir.resolve(".ninstall-icons");
                    var ext = IO.extension(res.name());
                    var icn = dir.resolve(shortcut.name() + "_" + (locale == null ? "default" : locale.toLanguageTag()) + ext);
                    try(var in = res.in()) {
                        checkParentDir(icn);
                        try(var out = Files.newOutputStream(icn)) {
                            in.transferTo(out);
                        }
                        journal.log(installdir.relativize(icn));
                        return icn.toAbsolutePath().toString();
                    }
            	});
                
            default:
                break;
            }
        }
        throw new UnsupportedOperationException("No suitable icon type for this platform.");
        
    }

    @Override
    public Path resolveShortcut(InstallStepContext context, Shortcut shortcut) {
        switch (context.setup().scope()) {
        case GLOBAL:
            return Paths.get("/usr/share/applications").resolve(shortcut.name() + ".desktop");
        default:
            return IO.home().resolve(".local").resolve("share").resolve("applications")
                    .resolve(shortcut.name() + ".desktop");
        }
    }

    @Override
    public void removeShortcut(Path path) {
        ioRun(() -> Files.deleteIfExists(path));
    }

	@Override
	public void createService(InstallStepContext context, Service shortcut) throws IOException {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Path resolveService(InstallStepContext context, Service shortcut) throws IOException {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void removeService(Path path) {
		throw new UnsupportedOperationException("TODO");		
	}

}
