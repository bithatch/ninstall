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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import uk.co.bithatch.ninstall.lib.Where.Layout;

public final class SDK {
	
	private static final String JAVA = "java";

	public final static SDK java(Path base) {
		return new Builder(base).build();
	}
	
	public final static class Builder {

		private Optional<String> candidate = Optional.empty();
		private Optional<String> identifier = Optional.empty();
		private final Path base;
		
		public Builder(Path base) {
			this.base = base;
		}
		
		public Builder withCandidate(String candidate) {
			this.candidate = Optional.of(candidate);
			return this;
		}
		
		public Builder withIdentifier(String identifier) {
			this.identifier = Optional.of(identifier);
			return this;
		}
				
		
		public SDK build() {
			return new SDK(this);
		}
	}

	private final Optional<String>  candidate;
	private final Optional<String> identifier;
	private final Path base;

	private SDK(Builder builder) {
		this.candidate = builder.candidate;
		this.identifier = builder.identifier;
		this.base = builder.base;
	}
	
	public Path command(Machine target, Layout layout, String name) {
		var candidatePath = candidate(target, layout);
		var home = candidatePath.resolve(identifier(target, layout));
		return home.resolve("bin").resolve(target.os().executable(name));
	}

	public String identifier(Layout layout) {
		return identifier(Machine.hostMachine(), layout);
	}

	public String identifier(Machine target, Layout layout) {
		var candidatePath = candidate(target, layout);
		return identifier.orElseGet(() -> 
			/* TODO sort semantically? */
			IO.ioCall(() -> Files.list(candidatePath).findFirst().map(Path::toString).orElseThrow(() -> new IllegalStateException("")))
		);
	}

	public Path candidate(Layout layout) {
		return candidate(Machine.hostMachine(), layout);
	}

	public Path candidate(Machine target, Layout layout) {
		return base.resolve(Locations.SDKS.resolve(target, layout)).resolve(candidate.orElse(JAVA));
	}

	public Path command(Layout layout, String name) {
		return command(Machine.hostMachine(), layout, name);
	}


}
