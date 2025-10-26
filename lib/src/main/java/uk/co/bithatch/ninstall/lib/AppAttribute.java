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
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public enum AppAttribute implements AttributeKey {

    NAME;
    
    public static final String APP_PROPERTIES = ".app.properties";

	public final static Map<AppAttribute, String> load(Path appDir) {
    	var props = IO.properties(appDir.resolve(APP_PROPERTIES));
        var map = new HashMap<AppAttribute, String>();
        props.forEach((k,v) -> {
            try {
                map.put(AppAttribute.valueOf((String)k), (String)v);
            }
            catch(IllegalArgumentException iae) {
            }
        });
        return map;
    }

    public final static void save(Path appDir, Map<AttributeKey, Object> attributes) {
        var appAttrFile = appDir.resolve(APP_PROPERTIES);
        var props = new Properties();
        var appAttrs = AppAttribute.values();
        for(var attr : appAttrs) {
            if(attributes.containsKey(attr)) {
                props.setProperty(attr.name(), (String)attributes.get(attr));
            }
        }
        try(var in = Files.newBufferedWriter(appAttrFile)) {
            props.store(in, "Ninstall App");
        }
        catch(IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }
    
}
