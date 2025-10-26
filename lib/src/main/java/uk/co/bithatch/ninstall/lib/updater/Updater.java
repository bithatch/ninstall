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
package uk.co.bithatch.ninstall.lib.updater;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sshtools.liftlib.IElevator;

import uk.co.bithatch.ninstall.lib.AppAttribute;
import uk.co.bithatch.ninstall.lib.AttributeKey;
import uk.co.bithatch.ninstall.lib.Mode;
import uk.co.bithatch.ninstall.lib.Registry;
import uk.co.bithatch.ninstall.lib.Scope;
import uk.co.bithatch.ninstall.lib.SetupApp;
import uk.co.bithatch.ninstall.lib.SetupAppOptions;
import uk.co.bithatch.ninstall.lib.uninstaller.Uninstaller;

public final class Updater extends SetupApp<UpdateContext, UpdaterToolkit> {

    public final static class Builder extends SetupApp.Builder<Updater, Builder, UpdateContext> {
        
        private URL url;
        
        public Builder withURL(String url) {
            try {
				return withURL(URI.create(url).toURL());
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException(e);
			}
        }
        
        public Builder withURL(URL url) {
            this.url = url;
            return this;
        }

        public Updater build() {
            return new Updater(this);
        }
    }
    
    private URL url;

    private Updater(Builder bldr) {
        super(bldr, UpdaterToolkit.class);
        if(bldr.url == null)
            throw new IllegalStateException("URL must be set.");
        this.url = bldr.url;
    }

    @Override
    protected void beforeSetup() {
        var ctx = context();
        ctx.attributes().putAll(AppAttribute.load(ctx.installLocation()));
    }

    @Override
    protected UpdateContext createContext(Scope scope, Registry registry, SetupAppOptions options, Mode mode) {
        var installLocation = registry.get(appId()).orElseThrow(() -> new IllegalStateException("Application ID is not set."));
        return new UpdateContext() {

            protected final Map<AttributeKey, Object> attrs = new HashMap<>();

            @Override
            public Path installLocation() {
                return installLocation;
            }
            
            @Override
            public Map<AttributeKey, Object> attributes() {
                return attrs;
            }

            @Override
            public Updater setupApp() {
                return Updater.this;
            }

            @Override
            public Registry registry() {
                return registry;
            }

            @Override
            public Scope scope() {
                return scope;
            }

            @Override
            public SetupAppOptions options() {
                return options;
            }

            @Override
            public List<AttributeKey> keys() {
                return Arrays.asList(UpdaterAttribute.values());
            }

            @Override
            public Mode mode() {
                return mode;
            }

			@Override
			public IElevator elevator() {
				return Updater.this.elevator();
			}
        };
    }
    
    public URL url() {
        return url;
    }

    @Override
    public String name() {
        return (String)context().attributes().getOrDefault(AppAttribute.NAME, "Unknown Application");
    }

}
