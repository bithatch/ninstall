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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SDKMan {

	public final static class Content {
		private InputStream input;
		private String contentType;
		private long contentSize;
		private String filename;

		public Content(InputStream input, String contentType, long contentSize, String filename) {
			super();
			this.input = input;
			this.contentType = contentType;
			this.contentSize = contentSize;
			this.filename = filename;
		}

		public InputStream input() {
			return input;
		}

		public String contentType() {
			return contentType;
		}

		public long contentSize() {
			return contentSize;
		}

		public String filename() {
			return filename;
		}
	}
	
	public interface Package {

		Content download();

		Content download(Platform platform);

		Path downloadTo(Path path);

		Path downloadTo(Platform platform, Path path);

		String version();
		
		String candidate();

		String displayName();

		String identifier();
		
	}

	public interface CandidateVersion extends Package {

		String distribution();

		String identifier();

		boolean validate();

		boolean validate(Platform platform);

		String vendor();
	}

	public interface Candidate extends Package {
		Stream<CandidateVersion> available();

		Stream<CandidateVersion> available(Platform platform);

		String description();

		String name();

		URI url();

		boolean validate();

		boolean validate(Platform platform);

		List<String> versions();

		List<String> versions(Platform platform);
	}

	private final static class CandidateImpl implements Candidate {
		private final String candidate;
		private final String name;
		private final String version;
		private final URI url;
		private final String description;
		private final SDKMan sdks;

		private CandidateImpl(String candidate, String name, String version, URI url, String description, SDKMan sdks) {
			super();
			this.candidate = candidate;
			this.name = name;
			this.version = version;
			this.url = url;
			this.description = description;
			this.sdks = sdks;
		}

		@Override
		public String identifier() {
			return version();
		}

		@Override
		public Stream<CandidateVersion> available() {
			return sdks.available(candidate);
		}

		@Override
		public Stream<CandidateVersion> available(Platform platform) {
			return sdks.available(candidate, platform);
		}

		@Override
		public String description() {
			return description;
		}

		@Override
		public Content download() {
			return sdks.download(candidate, version);
		}

		@Override
		public Content download(Platform platform) {
			return sdks.download(candidate, version, platform);
		}

		@Override
		public Path downloadTo(Path path) {
			return sdks.downloadTo(candidate, version, path);
		}

		@Override
		public Path downloadTo(Platform platform, Path path) {
			return sdks.downloadTo(candidate, version, platform, path);
		}

		@Override
		public String candidate() {
			return candidate;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public String toString() {
			return "CandidateImpl [candidate=" + candidate + ", name=" + name + ", version=" + version + ", url=" + url
					+ ", description=" + description + "]";
		}

		@Override
		public URI url() {
			return url;
		}

		@Override
		public boolean validate() {
			return sdks.validate(candidate, version);
		}

		@Override
		public boolean validate(Platform platform) {
			return sdks.validate(candidate, version, platform);
		}

		@Override
		public String version() {
			return version;
		}

		@Override
		public List<String> versions() {
			return sdks.versions(candidate);
		}

		@Override
		public List<String> versions(Platform platform) {
			return sdks.versions(candidate, platform);
		}

		@Override
		public String displayName() {
			return name + " (" + version + ")";
		}

	}

	private final static class CandidateVersionImpl implements CandidateVersion {
		private final String candidate;
		private final String vendor;
		private final String version;
		private final String distribution;
		private final String identifier;
		private final SDKMan sdks;

		private CandidateVersionImpl(String candidate, String vendor, String version, String distribution,
				String identifier, SDKMan sdks) {
			super();
			this.sdks = sdks;
			this.candidate = candidate;
			this.vendor = vendor;
			this.version = version;
			this.distribution = distribution;
			this.identifier = identifier;
		}

		@Override
		public String displayName() {
			return candidate + " (" + identifier + ")";
		}

		public String candidate() {
			return candidate;
		}

		public String distribution() {
			return distribution;
		}

		@Override
		public Content download() {
			return sdks.download(candidate, identifier);
		}

		@Override
		public Content download(Platform platform) {
			return sdks.download(candidate, identifier, platform);
		}

		@Override
		public Path downloadTo(Path path) {
			return sdks.downloadTo(candidate, identifier, path);
		}

		@Override
		public Path downloadTo(Platform platform, Path path) {
			return sdks.downloadTo(candidate, identifier, path);
		}

		public String identifier() {
			return identifier;
		}

		@Override
		public String toString() {
			return "CandidateVersionImpl [candidate=" + candidate + ", vendor=" + vendor + ", version=" + version
					+ ", distribution=" + distribution + ", candidate=" + identifier + "]";
		}

		@Override
		public boolean validate() {
			return sdks.validate(candidate, identifier);
		}

		@Override
		public boolean validate(Platform platform) {
			return sdks.validate(candidate, identifier, platform);
		}

		public String vendor() {
			return vendor;
		}

		public String version() {
			return version;
		}
	}

	public enum Platform {
		LINUXX64, LINUXX32, LINUXARM64, LINUXARM32SF, LINUXARM32HF, DARWINX64, DARWINARM64, WINDOWSX64, LINUX, LINUX64,
		LINUX32, DARWIN, FREEBSD, SUNOS, LEGACYWINDOWSPATTERN, EXOTIC;

		public String toId() {
			return name().toLowerCase();
		}
	}

	private final static String API_URI = "https://api.sdkman.io/2";
	// /candidates/list

	// https://api.sdkman.io/2/candidates/list
//	https://api.sdkman.io/2/candidates/java/linuxx64/versions/list?installed=

	// https://api.sdkman.io/2/candidates/maven/win/versions/all

//	  def apply(platformId: String): Platform = platformId.toLowerCase match {
//    case "linuxx64"              => LinuxX64
//    case "linuxx32"              => LinuxX32
//    case "linuxarm64"            => LinuxARM64
//    case "linuxarm32sf"          => LinuxARM32SF
//    case "linuxarm32hf"          => LinuxARM32HF
//    case "darwinx64"             => MacX64
//    case "darwinarm64"           => MacARM64
//    case "windowsx64"            => Windows64
//    case "linux"                 => LinuxX64
//    case "linux64"               => LinuxX64
//    case "linux32"               => LinuxX32
//    case "darwin"                => MacX64
//    case "freebsd"               => FreeBSD
//    case "sunos"                 => SunOS
//    case LegacyWindowsPattern(c) => Windows64
//    case _                       => Exotic
//  } 

	private static <T> T httpReader(String path, Function<BufferedReader, T> in) {
		return httpStream(path, fin -> {
			try {
				return in.apply(new BufferedReader(new InputStreamReader(fin, "UTF-8")));
			} catch (UnsupportedEncodingException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	private static <T> T httpStream(String path, Function<InputStream, T> in) {
		return http(path, (uri, resp) ->
			in.apply(resp.body())
		);
	}

	private static <T> T http(String path, BiFunction<URI, HttpResponse<InputStream>, T> in) {
		var uri = URI.create(API_URI + path);
		var client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).
				connectTimeout(Duration.ofSeconds(20)).build();
		
		/* We need to handle redirects ourselves, as we need the URI to extract filename from
		 * on a download
		 */
		
		for(int i = 0; i < 10 ; i++) {
			try {
				var request = HttpRequest.newBuilder().uri(uri).timeout(Duration.ofMinutes(2)).GET().build();
				var response = client.send(request, BodyHandlers.ofInputStream());
				if (response.statusCode() == 302) {
					uri = URI.create(response.headers().firstValue("Location").orElseThrow(() -> new IllegalStateException("No redirect location.")));
				}
				else if (response.statusCode() == 200) {
					return in.apply(uri, response);
				} else {
					throw new IOException("Unexpected response code " + response.statusCode());
				}
			} catch (InterruptedException ioe) {
				throw new IllegalStateException("Interrupted.", ioe);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		throw new IllegalStateException("To many redirects.");
	}

	public Optional<CandidateVersion> version(String candidate, String identifier) {
		return version(candidate, platform(), identifier);
	}

	public Optional<CandidateVersion> version(String candidate, Platform platform, String identifier) {
		return available(candidate, platform).filter(cv -> cv.identifier().equals(identifier)).findFirst();
	}

	public Stream<CandidateVersion> available(String candidate) {
		return available(candidate, platform());
	}

	public Stream<CandidateVersion> available(String candidate, Platform platform) {
		// https://api.sdkman.io/2/candidates/java/windowsx64/versions/list?installed=
		return httpReader("/candidates/" + candidate + "/" + platform.toId() + "/versions/list?installed=", rdr -> {
			return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<CandidateVersion>() {

				CandidateVersion next;
				boolean started = false;
				String vendor = null;

				private void checkNext() {
					if (next == null) {
						try {
							while (next == null) {
								var ln = rdr.readLine();
								if (ln == null)
									break;
								if (ln.startsWith("----------------------------------")) {
									started = true;
								} else if (started) {
									var parts = Arrays.asList(ln.split("\\|")).stream().map(String::trim).toList();
									var nextVendor = parts.get(0);
									if (!nextVendor.equals("")) {
										vendor = nextVendor;
									}
									next = new CandidateVersionImpl(candidate, vendor, parts.get(2), parts.get(3),
											parts.get(5), SDKMan.this);
								}

							}
						} catch (IOException ioe) {
							throw new UncheckedIOException(ioe);
						} catch (Exception e) {
						}
					}
				}

				@Override
				public boolean hasNext() {
					checkNext();
					return next != null;
				}

				@Override
				public CandidateVersion next() {
					checkNext();
					if (next == null)
						throw new IllegalStateException();
					try {
						return next;
					} finally {
						next = null;
					}
				}
			}, Spliterator.ORDERED), false).onClose(() -> {
				try {
					rdr.close();
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		});
	}

	public List<String> candidates() {
		return httpReader("/candidates/all", rdr -> {
			try {
				return Arrays.asList(rdr.readLine().split(","));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	public Content download(String candidate) {
		return get(candidate)
				.orElseThrow(() -> new IllegalArgumentException("No such candidate as `" + candidate + "`")).download();
	}

	public Content download(String candidate, Platform platform) {
		return get(candidate)
				.orElseThrow(() -> new IllegalArgumentException("No such candidate as `" + candidate + "`"))
				.download(platform);
	}

	public Path downloadTo(String candidate, Path path) {
		return get(candidate).orElseThrow(() -> new IllegalArgumentException("No such candidate as `" + candidate + "`"))
				.downloadTo(path);
	}

	public Path downloadTo(String candidate, Platform platform, Path path) {
		return get(candidate).orElseThrow(() -> new IllegalArgumentException("No such candidate as `" + candidate + "`"))
				.downloadTo(platform, path);
	}

	public Content download(String candidate, String identifier) {
		return download(candidate, identifier, platform());
	}

	public Content download(String candidate, String identifier, Platform platform) {
		// https://api.sdkman.io/2/broker/download/java/11.0.14.1-jbr/linuxx64
		return http("/broker/download/" + candidate + "/" + identifier + "/" + platform.toId(), (uri, resp) -> {
			var in = resp.body();
			var cdispOr = resp.headers().firstValue("Content-Disposition");
			
			String filename = null;
			if(cdispOr.isPresent()) {
				var cdisp =  cdispOr.get();
				if(cdisp.toLowerCase().startsWith("attachment")) {
					var idx = cdisp.indexOf("filename=");
					if(idx != -1) {
						var eidx = cdisp.indexOf(' ', idx + 1);
						if(eidx == -1) {
							eidx = cdisp.lastIndexOf('"');
							if(eidx == -1) {
								eidx = cdisp.length();
							}
						}
						filename = cdisp.substring(idx + 9, eidx);
						if(filename.startsWith("\""))
							filename = filename.substring(1);
						if(filename.endsWith("\""))
							filename = filename.substring(0, filename.length() -1);
					}
				}
			}
			
			if(filename == null) {
				var path = uri.getPath();
				var idx = path.lastIndexOf('/');
				filename = idx == -1 ? path : path.substring(idx + 1);
			}
			
			return new Content(in,
					resp.headers().firstValue("Content-Type").orElse("application/octet-stream"),
					resp.headers().firstValueAsLong("Content-Length").orElse(-1l),
					filename); 
		});
	}

	public Path downloadTo(String candidate, String version, Path path) {
		return downloadTo(candidate, version, platform(), path);
	}

	public Path downloadTo(String candidate, String version, Platform platform, Path path) {
		// https://api.sdkman.io/2/broker/download/java/11.0.14.1-jbr/linuxx64
		var dload = download(candidate, version, platform);
		try (var in = dload.input()) {
			if(Files.isDirectory(path)) {
				path = path.resolve(dload.filename);
			}			
			try (var out = Files.newOutputStream(path)) {
				in.transferTo(out);
				return path;
			}
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	public Optional<Candidate> get(String candidate) {
		return list().filter(f -> f.candidate().equals(candidate)).findFirst();
	}

	public Stream<Candidate> list() {
		return httpReader("/candidates/list", rdr -> {
			return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<Candidate>() {

				Candidate next;

				private void checkNext() {
					if (next == null) {
						try {
							boolean pkgNext = false;
							while (next == null) {
								var ln = rdr.readLine();
								if (ln == null)
									break;
								if (ln.startsWith("----------------------------------")) {
									pkgNext = true;
								} else if (pkgNext) {
									var eidx = ln.lastIndexOf(' ');
									var lidx = ln.lastIndexOf(')');
									var zidx = Math.max(eidx, lidx);
									var url = ln.substring(zidx + 1);
									var name = ln.substring(0, ln.lastIndexOf('(') - 1);
									var version = ln.substring(ln.lastIndexOf('(') + 1, ln.lastIndexOf(')'));
									rdr.readLine();
									var desc = new ArrayList<String>();
									while (!(ln = rdr.readLine()).equals("")) {
										desc.add(ln);
									}
									var description = String.join("\n", desc);
									var last = rdr.readLine();
									var id = last.substring(last.lastIndexOf(' ') + 1);
									pkgNext = false;
									next = new CandidateImpl(id, name, version, URI.create(url), description,
											SDKMan.this);
								}

							}
						} catch (IOException ioe) {
							throw new UncheckedIOException(ioe);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				@Override
				public boolean hasNext() {
					checkNext();
					return next != null;
				}

				@Override
				public Candidate next() {
					checkNext();
					if (next == null)
						throw new IllegalStateException();
					try {
						return next;
					} finally {
						next = null;
					}
				}
			}, Spliterator.ORDERED), false).onClose(() -> {
				try {
					rdr.close();
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		});
	}

	public Platform platform() {
		return Machine.hostMachine().sdkmanPlatform();
	}

	public boolean validate(String candidate, String version) {
		return validate(candidate, version, platform());
	}

	public boolean validate(String candidate, String version, Platform platform) {
		// https://api.sdkman.io/2/candidates/validate/java/11.0.11.1-jbr/linuxx64
		return httpReader("/candidates/validate/" + candidate + "/" + version + "/" + platform.toId(), rdr -> {
			try {
				return rdr.readLine().equals("valid");
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	public List<String> versions(String identifier) {
		return versions(identifier, platform());
	}

	public List<String> versions(String identifier, Platform platform) {
		// https://api.sdkman.io/2/candidates/java/windowsx64/versions/all
		return httpReader("/candidates/" + identifier + "/" + platform.toId() + "/versions/all", rdr -> {
			try {
				return Arrays.asList(rdr.readLine().split(","));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
}
