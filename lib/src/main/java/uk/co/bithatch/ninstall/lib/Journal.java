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

import static uk.co.bithatch.ninstall.lib.IO.ioRun;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class Journal implements Closeable {

	public final static class Builder {
		private final String name;
		private final SetupAppContext<?, ?> context;
		private Optional<Stash> stash = Optional.empty();
		private Optional<Consumer<Journal>> onDelete = Optional.empty();

		public Builder(SetupAppContext<?, ?> context, String name) {
			this.name = name;
			this.context = context;
		}

		public Builder onDelete(Consumer<Journal> onDelete) {
			this.onDelete = Optional.of(onDelete);
			return this;
		}

		public Builder withStash(Stash stash) {
			this.stash = Optional.of(stash);
			return this;
		}

		public Journal build() {
			return new Journal(this);
		}
	}

	private final String name;
	private final Path journal;
	private final SetupAppContext<?, ?> context;
	private final Optional<Stash> stash;
	private final Optional<Consumer<Journal>> onDelete;

	private int logs;

	private Journal(Builder builder) {
		context = builder.context;
		name = builder.name;
		stash = builder.stash;
		onDelete = builder.onDelete;

		journal = context.installLocation().resolve("." + name + ".jnl");
	}

	public Stream<Path> paths() {
		var paths = new ArrayList<Path>();
		if (Files.exists(journal)) {
			try (var in = new BufferedReader(Files.newBufferedReader(journal))) {
				String line;
				while ((line = in.readLine()) != null) {
					paths.add(Paths.get(line));
				}
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		}
		return paths.stream().distinct().sorted(Path::compareTo).sorted(Comparator.reverseOrder());
	}

	public void log(Path path) {
		if (logs == 0 && Files.exists(journal)) {
			stash.ifPresent(s -> {
				s.stash(journal);
			});
		}

		try (var out = new PrintWriter(
				Files.newBufferedWriter(journal, StandardOpenOption.CREATE, StandardOpenOption.APPEND), true)) {
			out.println(path.toString());
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		} finally {
			logs++;
		}
	}

	public void delete() {
		ioRun(() -> Files.deleteIfExists(journal));
		stash.ifPresent(s -> s.unstash(journal));
		logs = 0;
		onDelete.ifPresent(od -> od.accept(this));
	}

	public int size() {
		return logs;
	}

	@Override
	public void close() {
		logs = 0;
		if (Files.exists(journal)) {
			try (var out = new PrintWriter(Files.newBufferedWriter(journal, StandardOpenOption.CREATE), true)) {
				paths().forEach(p -> out.println(p.toString()));
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			} finally {
				logs++;
			}
		}
	}

	public Stash stash() {
		return stash.orElseThrow(() -> new IllegalStateException("Journal `" + name + "` has no stash"));
	}

	public String name() {
		return name;
	}
}
