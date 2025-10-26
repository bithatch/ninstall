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
import java.util.Optional;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Registry {
    
    private Scope scope;

    public Registry(Scope scope) {
        this.scope = scope;
    }

    public Optional<Path> get(UUID appId) {
        var prefs = prefForApp(appId);
        var loc = prefs.get("location", "");
        if(loc.equals("")) {
            return Optional.empty();
        }
        else {
            return Optional.of(Paths.get(loc));
        }
    }

    public void deregister(UUID appId) {
        var prefs = prefForApp(appId);
        try {
            prefs.removeNode();
        } catch (BackingStoreException e) {
            throw new IllegalStateException(e);
        }
    }
    
    public void register(UUID appId, Path installLocation) {
        var prefs = prefForApp(appId);
        prefs.put("location", installLocation.toAbsolutePath().toString());
    }

    private Preferences prefForApp(UUID appId) {
        var appIdStr = appId.toString();
        var root = prefs();
        var prefs = root.node(appIdStr);
        return prefs;
    }
    
    private Preferences prefs() {
        switch(scope) {
        case GLOBAL:
            return Preferences.systemRoot().node("uk/co/bithatch/ninstall/installed");
        default:
            return Preferences.userRoot().node("uk/co/bithatch/ninstall/installed");
        }
    }
}
