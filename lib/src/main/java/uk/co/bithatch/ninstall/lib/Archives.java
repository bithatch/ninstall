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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

public class Archives {

	public enum Format {
		TAR_GZ, TAR_BZ2, TAR, ZIP;

		public static Format fromPath(Path path) {
			return fromFilename(path.getFileName().toString());
		}

		public static Format fromFilename(String filename) {
			var bfn = filename.toLowerCase();
			if (bfn.endsWith(".tar.gz")) {
				return TAR_GZ;
			} else if (bfn.endsWith(".tar.bz2")) {
				return TAR_BZ2;
			} else if (bfn.endsWith(".tar")) {
				return TAR;
			} else if (bfn.endsWith(".zip")) {
				return ZIP;
			} else {
				throw new UnsupportedOperationException("Unspported archive format. " + filename);
			}
		}
	}

	public static List<Path> extract(Format format, InputStream in, Path destination) {
		try {
			var tais = open(format, in);
			var entry = tais.getNextEntry();
			var paths = new ArrayList<Path>();
			while (entry != null) {
				var name = entry.getName();
				while (name.startsWith("/")) {
					name = name.substring(1);
				}
				if (name.equals("")) {
					continue;
				}

				while (name.endsWith("/"))
					name = name.substring(0, name.length() - 1);

				var des = destination.resolve(Paths.get(name.replace('/', File.separatorChar)));
				var lnk = getLink(des, tais, entry);
				if (lnk.isPresent()) {
					Files.createSymbolicLink(des, Paths.get(lnk.get()));
				} else {
					if (entry.isDirectory()) {
						Files.createDirectories(des);
						tais.transferTo(OutputStream.nullOutputStream());
					} else {
						var parent = des.getParent();
						if (parent != null && !Files.exists(parent))
							Files.createDirectories(parent);

						try (var out = Files.newOutputStream(des)) {
							tais.transferTo(out);
						}
					}
					setAttributes(des, entry);
				}

				entry = tais.getNextEntry();
				paths.add(des);
			}
			return paths;
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		} catch (CompressorException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static Optional<String> getLink(Path des, ArchiveInputStream<?> in, ArchiveEntry entry) throws IOException {
		if (entry instanceof TarArchiveEntry tae && tae.isSymbolicLink()) {
			in.transferTo(OutputStream.nullOutputStream());
			return Optional.of(tae.getLinkName());
		} else if (entry instanceof ZipArchiveEntry za && za.isUnixSymlink()) {
			var bout = new ByteArrayOutputStream();
			in.transferTo(bout);
			return Optional.of(new String(bout.toByteArray(), "UTF-8"));
		}
		return Optional.empty();
	}

	private static void setAttributes(Path des, ArchiveEntry entry) throws IOException {
		Files.setLastModifiedTime(des, FileTime.fromMillis(entry.getLastModifiedDate().getTime()));
		if (entry instanceof TarArchiveEntry tae) {
			setUnixMode(des, tae.getMode());
		} else if (entry instanceof ZipArchiveEntry zae) {
			var mode = zae.getUnixMode();
			if (mode > 0)
				setUnixMode(des, mode);
			else {
				Boolean read = null;
				Boolean write = null;
				Boolean execute = null;
				var extra = zae.getExtra() == null ? "" : new String(zae.getExtra(), "UTF-8");
				char[] extrac = extra.toCharArray();
				for (int i = 0; i < extrac.length; i++) {
					char c = extrac[i];
					switch (c) {
					case 'r':
						read = false;
						break;
					case 'R':
						read = true;
						break;
					case 'w':
						write = false;
						break;
					case 'W':
						write = true;
						break;
					case 'x':
						execute = false;
						break;
					case 'X':
						execute = true;
						break;
					}
				}

				if (read != null) {
					des.toFile().setReadable(read);
				}

				if (write != null) {
					des.toFile().setWritable(write);
				}

				if (execute != null) {
					des.toFile().setExecutable(execute);
				}
			}
		}
	}

	private static void setUnixMode(Path des, int mode) {
		if (mode > 0) {
			var perms = IO.fromBitmask(mode);
			try {
				try {
					Files.setPosixFilePermissions(des, perms);
				} catch (UnsupportedOperationException uoe) {
					var fl = des.toFile();
					fl.setExecutable(perms.contains(PosixFilePermission.OWNER_EXECUTE),
							perms.contains(PosixFilePermission.GROUP_EXECUTE)
									|| perms.contains(PosixFilePermission.OTHERS_EXECUTE));
					fl.setWritable(perms.contains(PosixFilePermission.OWNER_WRITE),
							perms.contains(PosixFilePermission.GROUP_WRITE)
									|| perms.contains(PosixFilePermission.OTHERS_WRITE));
					fl.setReadable(perms.contains(PosixFilePermission.OWNER_READ),
							perms.contains(PosixFilePermission.GROUP_READ)
									|| perms.contains(PosixFilePermission.OTHERS_READ));
				}
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		}
	}

	private static ArchiveInputStream<? extends ArchiveEntry> open(Format format, InputStream in)
			throws CompressorException {
		switch (format) {
		case TAR_BZ2:
			return new TarArchiveInputStream(
					new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.BZIP2, in));
		case TAR_GZ:
			return new TarArchiveInputStream(
					new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, in));
		case TAR:
			return new TarArchiveInputStream(in);
		case ZIP:
			return new ZipArchiveInputStream(in);
		default:
			throw new UnsupportedOperationException("Unspported archive format. " + format);
		}
	}

	public static void putNextEntry(ZipOutputStream zipOut, ZipEntry zipEntry, Path path) throws IOException {
		var extra = new StringBuilder();

		extra.append(Files.isReadable(path) ? "R" : "r");
		extra.append(Files.isWritable(path) ? "W" : "w");
		extra.append(Files.isExecutable(path) ? "X" : "x");

		if (Files.isSymbolicLink(path)) {
			var lpath = Files.readSymbolicLink(path);
			var lpathstr = lpath.toString();
			extra.append("L");
			extra.append(String.format("%04d", lpathstr.length()));
			extra.append(lpathstr);
		}

		zipEntry.setExtra(extra.toString().getBytes("UTF-8"));
		zipOut.putNextEntry(zipEntry);
	}
}
