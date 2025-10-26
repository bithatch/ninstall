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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.sshtools.liftlib.OS;

import uk.co.bithatch.ninstall.lib.packaging.Packager;

public interface Resource {

	public enum Source {
		NAMED, INSTALLED, BUNDLED, STATIC
	}

	Source source();

	InputStream in();

	String name();
	
	default Charset encoding() {
		return Charset.defaultCharset();
	}

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

	public static Resource ofBundle(String path) {
		return ofBundle(null, path);
	}
	

	public static Resource ofBundle(Path path) {
		return ofBundle(null, path);
	}

	public static Resource ofBundle(String bundlePath, String path) {
		return ofBundle(bundlePath, Paths.get(path));
	}

	public static List<Resource> ofBundles(String bundlePath, String... path) {
		return Arrays.asList(path).stream().map(p -> ofBundle(bundlePath, p)).toList();
	}

	public static List<Resource> ofBundles(String bundlePath, Path... path) {
		return Arrays.asList(path).stream().map(p ->   ofBundle(bundlePath, p)).toList();
	}

	public static Resource ofBundle(String bundlePath, Path path) {
		
		/* How a bundled resource is dealt with depends on where it is being loaded. If
		 * it loading directly, i.e. the developer is testing their installer, then well
		 * simple load the resource directly from the given path.
		 * 
		 * When it is  loaded in the context of a package build, then we also register this
		 * resource for inclusion in  the package.
		 * 
		 * If it is loading in the context of an  natively, then we assume
		 * the resource will be in a path relative to it, i.e. [bundlePath/[path.fileName]
		 */
		
		var res = new Resource() {
			@Override
			public InputStream in() {
				if(OS.isNativeImage()) {
					/* TODO make relative to the executable. possible to do with plain Java with 
					 * some caveats (Process handle stuff)
					 */
					if(bundlePath == null)
						return ioCall(() -> Files.newInputStream(path.getFileName()));
					else
						return ioCall(() -> Files.newInputStream(Paths.get(bundlePath, path.getFileName().toString())));
				}
				else {
					return ioCall(() -> Files.newInputStream(path));
				}
			}

			@Override
			public String name() {
				return bundlePath == null 
						? path.getFileName().toString()
						: Paths.get(bundlePath).resolve(path.getFileName().toString()).toString() ;
			}

			@Override
			public Source source() {
				return Source.BUNDLED;
			}
		};
		
		Packager.packagerContext().ifPresent(ctx -> {
			ctx.resource(res);
		});
		
		return res;
	}

	public static Resource ofResource(String name) {
		return new Resource() {
			@Override
			public InputStream in() {
				return ioCall(() -> {
					var ctxl = Thread.currentThread().getContextClassLoader();
					if (ctxl != null) {
						var in = ctxl.getResourceAsStream(name);
						if (in != null) {
							return in;
						}
					}

					var clzl = Resource.class.getClassLoader();
					var in = clzl.getResourceAsStream(name);
					if (in == null) {
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
					if (in == null) {
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

	public static Resource ofContent(String content) {
		return new Resource() {
			@Override
			public InputStream in() {
				return new ByteArrayInputStream(content.getBytes());
			}

			@Override
			public String name() {
				throw new IllegalStateException("No  name.");
			}

			@Override
			public Source source() {
				return Source.STATIC;
			}
		};
	}

	public static Resource ofContent(URL content) {
		return new Resource() {
			@Override
			public InputStream in() {
				try {
					var in = content.openStream();
					if (in == null)
						throw new NoSuchFileException(content.toURI().toString());
					return in;
				} catch (IOException ioe) {
					throw new UncheckedIOException(ioe);
				} catch (URISyntaxException e) {
					throw new IllegalArgumentException(content.toExternalForm());
				}
			}

			@Override
			public String name() {
				return content.toExternalForm();
			}

			@Override
			public Source source() {
				return Source.STATIC;
			}
		};
	}

	default String asString() {
		var baos = new ByteArrayOutputStream();
		try(var in = in()) {
			in.transferTo(baos);
			return new String(baos.toByteArray(), encoding());
		}
		catch(IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}
}
