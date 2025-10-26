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

import static uk.co.bithatch.ninstall.lib.IO.ioCall;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public interface Resource {
    
    public enum Source {
        NAMED, INSTALLED, BUNDLED
    }
    
    Source source();
    
    InputStream in();

    String name();


    public static List<Resource> ofResources(Class<?> base, String... names) {
    	return Arrays.asList(names).stream().map(n -> ofResource(base, n)).toList();
    }

    public static Resource ofNamed(String name) {
        return new Resource() {

            @Override
            public Source source() {
                return Source.NAMED;
            }

            @Override
            public InputStream in() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String name() {
                return name;
            }
            
        };
    }

    public static Resource ofInstalled(String path) {
        return new Resource() {

            @Override
            public Source source() {
                return Source.INSTALLED;
            }

            @Override
            public InputStream in() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String name() {
                return path;
            }
            
        };
    }

    public static Resource ofBundled(String path) {
        return ofBundled(Paths.get(path));
    }

    public static List<Resource> ofBundled(String... path) {
        return Arrays.asList(path).stream().map(Resource::ofBundled).toList();
    }

    public static List<Resource> ofBundled(Path... path) {
        return Arrays.asList(path).stream().map(Resource::ofBundled).toList();
    }
    
    public static Resource ofBundled(Path path) {
        return new Resource() {
            @Override
            public InputStream in() {
                return ioCall(() -> Files.newInputStream(path));
            }

            @Override
            public String name() {
                return path.toString();
            }

            @Override
            public Source source() {
                return Source.BUNDLED;
            }
        };
    }
    
    public static Resource ofResource(String name) {
        return new Resource() {
            @Override
            public InputStream in() {
                return ioCall(() -> {
                	var ctxl = Thread.currentThread().getContextClassLoader();
                	if(ctxl != null) {
    					var in = ctxl.getResourceAsStream(name);
                        if(in != null) {
                        	return in;
                        }
                	}
                		
                    var clzl = Resource.class.getClassLoader();
					var in = clzl.getResourceAsStream(name);
                    if(in == null) {
                        throw new NoSuchFileException(name);
                    }
                    return in;
                });
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public Source source() {
                return Source.BUNDLED;
            }
        };
    }

    public static Resource ofResource(Class<?> parent, String name) {
        return new Resource() {
            @Override
            public InputStream in() {
                return ioCall(() -> {
                    var in = parent.getResourceAsStream(name);
                    if(in == null) {
                        throw new NoSuchFileException(name);
                    }
                    return in;
                });
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public Source source() {
                return Source.BUNDLED;
            }
        };
    }
}
