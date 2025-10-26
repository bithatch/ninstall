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
package uk.co.bithatch.ninstall.lib.repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.NoSuchFileException;
import java.util.Optional;

import uk.co.bithatch.ninstall.lib.Http;
import uk.co.bithatch.ninstall.lib.Http.HttpClientFactory;


public final class MavenRemoteRepositoryImpl implements RemoteRepository {

	public final static class RemoteRepositoryBuilder implements RemoteRepository.RemoteRepositoryBuilder {
		private URI root = URI.create("https://repo1.maven.org/maven2");
		private String name = "Remote Repository";

		@Override
		public RemoteRepositoryBuilder withName(String name) {
			this.name = name;
			return this;
		}

		@Override
		public RemoteRepositoryBuilder withRoot(String root) {
			return withRoot(URI.create(root));
		}

		@Override
		public RemoteRepositoryBuilder withRoot(URI root) {
			this.root = root;
			return this;
		}

		@Override
		public RemoteRepository build() {
			return new MavenRemoteRepositoryImpl(this);
		}

		@Override
		public String id() {
			return "central";
		}
	}

	private final URI root;
	private final String name;
	private final String id;

	public MavenRemoteRepositoryImpl(RemoteRepositoryBuilder builder) {
		this.root = builder.root;
		this.name = builder.name;
		this.id = builder.id();
	}

	@Override
	public boolean supported(GAV gav) {
		return gav.repositoryOr().isEmpty() || gav.repository().equals(id());
	}

	@Override
	public Optional<ResolutionResult> resolve(HttpClientFactory factory, GAV gav) {
		// TODO classifier
		return Optional.of(ResolutionResult.of(URI.create(root.toString() + '/' + dottedToPath(gav.groupId()) + '/'
				+ gav.artifactId() + '/' + gav.version() + '/' + gav.artifactId() + "-" + gav.version() + ".jar")));
	}

	static String dottedToPath(String dotted) {
		return dotted.replace('.', File.separatorChar);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public InputStream download(HttpClientFactory httpClientFactory, GAV gav, URI uri, ResolutionResult result,
			Optional<ResolutionMonitor> monitor) throws IOException {
		var httpClient = httpClientFactory.get().build();
		var request = HttpRequest.newBuilder().GET().uri(uri).build();
		var handler = HttpResponse.BodyHandlers.ofInputStream();
		try {
			var response = httpClient.send(request, handler);
			switch (response.statusCode()) {
			case 200:
				monitor.ifPresent(m -> m.found(gav, uri, this, Http.contentLength(response)));
				return response.body();
			case 404:
				throw new NoSuchFileException(uri.toString());
			default:
				throw new IOException("Unexpected status " + response.statusCode());
			}
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String id() {
		return id;
	}
}
