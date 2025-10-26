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

import static uk.co.bithatch.ninstall.lib.IO.checkDir;
import static uk.co.bithatch.ninstall.lib.IO.checkParentDir;
import static uk.co.bithatch.ninstall.lib.IO.ioRun;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

public final class Stash implements Closeable {
    public final static ResourceBundle RESOURCES = ResourceBundle.getBundle(Stash.class.getName());

    public final static class Builder {
        private final SetupAppContext<?, ?> context;
        private final String name;

        public Builder(SetupAppContext<?, ?>  context, String name) {
            this.context = context;
            this.name = name;
        }
        
        public Stash build() {
            return new Stash(this);
        }

    }

    private final Path root;
    private final Path index;
    private final List<Path> created = new ArrayList<>();

    private record Item(UUID uuid, Path path) {
    }

    private Stash(Builder bldr) {
        root = bldr.context.tmp().resolve(bldr.name);
        index = root.resolve("index.log");
    }

	public void created(Path path) {
		if(!Files.exists(path)) {
			created.add(path);
		}
	}
    
    public void stashOrCreated(Path path) {
    	if(Files.exists(path)) {
    		stash(path);
    	}
    	else {
    		this.created.add(path);
    	}
    }

    public void stash(Path path) {
        stash(path, true);
    }
    
    public void stash(Path path, boolean recursive) {
        try {
            if(!Files.exists(path))
                throw new NoSuchFileException("Can only stash paths that exist. `" + path + "` does not.");
            checkDir(root);
            
            var absPath = path.toAbsolutePath().toString();
            var uuid = UUID.nameUUIDFromBytes(absPath.getBytes("UTF-8"));
            var stashed = root.resolve(uuid.toString());
            if(Files.exists(stashed))
                return;
            
            IO.moveAcrossStores(recursive, path, stashed);

            try (var out = new PrintWriter(Files.newBufferedWriter(index, StandardOpenOption.CREATE, StandardOpenOption.APPEND), true)) {
                out.println(String.format("%s %s", uuid, absPath));
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public synchronized void unstash(Path path) {
        var absPath = path.toAbsolutePath().toString();
        ioRun(() -> {
            var uuid = UUID.nameUUIDFromBytes(absPath.getBytes("UTF-8"));
            Files.deleteIfExists(root.resolve(uuid.toString()));
            if (Files.exists(index)) {
                var tempFile = index.getParent().resolve(index.getFileName().toString() + ".tmp");
                Files.deleteIfExists(tempFile);
                try(var wtrt = new PrintWriter(Files.newBufferedWriter(tempFile))) {
                    try (var rdr = Files.newBufferedReader(index)) {
                        String line;
                        while ((line = rdr.readLine()) != null) {
                            if(!line.startsWith(uuid.toString())) {
                                wtrt.println(line);
                            }
                        }
                    }
                } catch (IOException ioe) {
                    throw new UncheckedIOException(ioe);
                }
                IO.moveAcrossStores(tempFile, index, StandardCopyOption.REPLACE_EXISTING);
            }
        });
    }

    public void restoreAndClose(Progress progress) {
    	restoreAndClose(Optional.of(progress));
    }
    
    public void restoreAndClose(Optional<Progress> progress) {
    	created.forEach(f -> { 
            IO.delete(f);
           progress.ifPresent(p -> p.info(RESOURCES.getString("deleted"), f.toString()));
        });
        created.clear();
    	 
        var items = new ArrayList<Item>();
        if (Files.exists(index)) {
            try (var rdr = Files.newBufferedReader(index)) {
                String line;
                while ((line = rdr.readLine()) != null) {
                    var idx = line.indexOf(' ');
                    items.add(new Item(UUID.fromString(line.substring(0, idx)), Paths.get(line.substring(idx + 1))));
                }
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
            ioRun(() -> Files.delete(index));
        }
        Collections.reverse(items);
        for (var item : items) {
            ioRun(() -> IO.moveAcrossStores(root.resolve(item.uuid.toString()), checkParentDir(item.path()),
                    StandardCopyOption.REPLACE_EXISTING));
            progress.ifPresent(p -> p.info(RESOURCES.getString("restored"), item.path().getFileName()));
        }
        close();
    }

    @Override
    public void close()  {
    	
    	if(Files.exists(root))
    		IO.delete(root);
    	
        created.clear();
    }
}
