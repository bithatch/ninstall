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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sshtools.liftlib.IElevator;

public interface SetupAppContext<APP extends SetupApp<?, ?>, OPTS extends SetupAppOptions> {
    
    public final static class Default {
        private final static Path TMP = tmp();
        
        private static Path tmp() {
            try {
                return Files.createTempDirectory("setup");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
    
    List<AttributeKey> keys();

    OPTS options();

    APP setupApp();
    
    Scope scope();
    
    Registry registry();
    
    Mode mode();
    
    IElevator elevator();
    
    Map<AttributeKey, Object> attributes();
    
    @SuppressWarnings("unchecked")
	default <V> Optional<V> get(AttributeKey key, Class<V> type) {
    	return Optional.ofNullable((V)attributes().get(key));
    }
    
    default void set(AttributeKey key, Object value) {
    	attributes().put(key, value);
    }

    default Path resolve(Path outputPath) {
        if(outputPath.isAbsolute()) {
            return outputPath;
        }
        else {
            return installLocation().resolve(outputPath);
        }
    }
    
    default Path tmp() {
        return Default.TMP;
    }

    Path installLocation();
}
