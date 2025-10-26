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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public class IO {
	
	private final static boolean DELAYS = Boolean.getBoolean("ninstall.delays");
    
    public interface FileAttrs {
        void set(Path target) throws IOException;
    }

    @FunctionalInterface
    public interface IORun {
        void run() throws IOException;
    }

    @FunctionalInterface
    public interface IORunInDir {
        void run(Path dir) throws IOException;
    }

    @FunctionalInterface
    public interface IOCall<T> {
        T call() throws IOException;
    }

    @FunctionalInterface
    public interface IOCallInDir<T> {
        T call(Path dir) throws IOException;
    }
    

	public static Set<PosixFilePermission> fromBitmask(long mode) {
		var l = new LinkedHashSet<PosixFilePermission>();
		for (var perm : PosixFilePermission.values()) {
			if ((mode & toMask(perm)) != 0) {
				l.add(perm);
			}
		}
		return Collections.unmodifiableSet(l);
	}
	
	/**
	 * Get the bitmask flag value for a given permission.
	 * 
	 * @param permission permission
	 * @return bitmask flag value
	 */
	public static int toMask(PosixFilePermission permission) {
		switch (permission) {
		case OWNER_WRITE:
			return 0x80;
		case OWNER_EXECUTE:
			return 0x40;
		case GROUP_READ:
			return 0x20;
		case GROUP_WRITE:
			return 0x10;
		case GROUP_EXECUTE:
			return 0x08;
		case OTHERS_READ:
			return 0x04;
		case OTHERS_WRITE:
			return 0x02;
		case OTHERS_EXECUTE:
			return 0x02;
		default:
			return 0x100;
		}
	}
	
    public static Path checkParentDir(Path dirOrFile) {
        checkDir(dirOrFile.getParent());
        return dirOrFile;
    }

    public static Path checkDir(Path dir) {
        return ioCall(() -> {
            if (Files.exists(dir)) {
                if (!Files.isDirectory(dir))
                    throw new IOException(MessageFormat.format("''{0}'' is not a directory.", dir));

            } else {
                Files.createDirectories(dir);
            }
            return dir;
        });
    }

    public static void moveAcrossStores(Path from, Path to, CopyOption... options) {
        moveAcrossStores(true, from, to, options);
    }
    
    public static void moveAcrossStores(boolean recursive, Path from, Path to, CopyOption... options) {
        try {
            Files.move(from, to, options);
        }
        catch(DirectoryNotEmptyException dnee) {
            if(recursive) {
                copyDir(from, to, options);
                delete(from);
            }
            else
                throw new UncheckedIOException(dnee);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public static void copyDir(Path from, Path to, CopyOption... options) {
        try {
            Files.walk(from).forEach(source -> {
                Path destination = Paths.get(to.toString(), source.toString()
                  .substring(from.toString().length()));
                try {
                    Files.copy(source, destination, options);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public static void delete(Path path) {
        ioRun(() -> {
            try (var walk = Files.walk(path)) {
                walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
        });
    }

    public static void ioRun(IORun task) {
        try {
            task.run();
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public static <T> T ioCall(IOCall<T> task) {
        try {
            return task.call();
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public static void ioRunInTempDir(IORunInDir task) {
        try {
        	var tmpdir = Files.createTempDirectory("ninstall");
        	try {
        		task.run(tmpdir);
        	}
        	finally {
        		delete(tmpdir);
        	}
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public static <T> T ioCallInTempDir(IOCallInDir<T> task) {
        try {
        	var tmpdir = Files.createTempDirectory("ninstall");
        	try {
        		return task.call(tmpdir);
        	}
        	finally {
        		delete(tmpdir);
        	}
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public static void ioRunInTempDir(Path dir, IORunInDir task) {
        try {
        	var tmpdir = Files.createTempDirectory(dir, ".ninstall");
        	try {
        		task.run(tmpdir);
        	}
        	finally {
        		delete(tmpdir);
        	}
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public static <T> T ioCallInTempDir(Path dir, IOCallInDir<T> task) {
        try {
        	var tmpdir = Files.createTempDirectory(dir, ".ninstall");
        	try {
        		return task.call(tmpdir);
        	}
        	finally {
        		delete(tmpdir);
        	}
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public static String displayPath(Path path) {
        var realPath = path.toAbsolutePath();
        var realCwd = cwd().toAbsolutePath();
        if (realPath.startsWith(realCwd)) {
            return shortUserPath(realCwd.relativize(realPath));
        } else {
            return shortUserPath(path.toAbsolutePath());
        }
    }

    public static String shortUserPath(Path path) {
        var home = home();
        if (path.startsWith(home)) {
            return "~" + File.separator + home.relativize(path).toString();
        } else {
            return path.toString();
        }
    }
    
    public static Properties properties(Path path) {
    	return propertiesOr(path).orElseThrow(() -> new UncheckedIOException(new NoSuchFileException(path.toString())));
    }
    public static Optional<Properties> propertiesOr(Path path) {
    	if(Files.exists(path)) {
    		var p = new Properties();
    		try(var rdr = Files.newBufferedReader(path)) {
    			p.load(rdr);
    		}
    		catch(IOException ioe) {
    			throw new UncheckedIOException(ioe);
    		}
    		return Optional.of(p);
    	}
    	else
    		return Optional.empty();
    }

    public static Path home() {
        return Paths.get(System.getProperty("user.home"));
    }
    
    public static FileAttrs attrs(Path path) throws IOException {
        var posix = Files.getFileAttributeView(path, PosixFileAttributeView.class);
        if(posix == null) {
            throw new UnsupportedOperationException("TODO");
        }
        else {
            var perms = posix.readAttributes();
            var set = perms.permissions();
            var group = perms.group();
            var owner = perms.owner();
            var creation =perms.creationTime();
            var modified =perms.lastModifiedTime();
            var accessed  =perms.lastAccessTime();
            return new FileAttrs() {
                @Override
                public void set(Path target) throws IOException {
                    var targetPosix = Files.getFileAttributeView(target, PosixFileAttributeView.class);
                    targetPosix.setGroup(group);
                    targetPosix.setOwner(owner);
                    targetPosix.setPermissions(set);
                    targetPosix.setTimes(modified, accessed, creation);
                }
            };
        }
    }

    public static String extension(String name) {
        var idx = name.lastIndexOf('.');
        return idx == -1? "" : name.substring(idx);
    }
    
    public static void delay(long ms) {
    	if(DELAYS) {
    		try {
				Thread.sleep(ms);
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
    	}
    }

	public static String stripExtension(String path) {
		var idx = path.lastIndexOf('.');
		return idx == -1 ? path : path.substring(0, idx);
	}

	public static Path cwd() {
		return Paths.get(System.getProperty("user.dir"));
	}
	
}
