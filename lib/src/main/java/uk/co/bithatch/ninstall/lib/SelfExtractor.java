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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SelfExtractor {

	public static final String EXEC = "exec";
	public static final String NAME = "name";
    public static final String VERSION = "version";

	static void printHelp(String err, String name) {
		System.err.println("Self Extractor.");
		if (err != null)
			System.err.println(err);
		System.err.println();
		System.err.println("Options:-");
		System.err.println("    --no-cleanup    Do not remove temporary files when complete.");
		System.err.println("    --no-exec       Do not execute installer script.");
		System.err.println("    --quiet         No output.");
		System.err.println("    --verbose       Verbose output.");
		System.err.println("    --              Pass any subsequent options to the installer.");
	}

	public static void main(String[] args) throws Exception {

		var props = new Properties();
		try (var in = SelfExtractor.class.getResourceAsStream("/data.properties")) {
			props.load(in);
		}
		var name = props.getProperty(NAME, "Application");
        var version = props.getProperty(VERSION, "Unknown version");

		/* Config */
		boolean cleanup = true;
		boolean exec = true;
		boolean quiet = false;
		boolean verbose = false;
		List<String> installerArgs = new ArrayList<>();

		/* Parse arguments */
		try {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("--no-cleanup")) {
					cleanup = false;
				} else if (args[i].equals("--no-exec")) {
					exec = false;
				} else if (args[i].equals("--verbose")) {
					verbose = true;
				} else if (args[i].equals("--quiet")) {
					quiet = true;
				} else if (args[i].equals("--")) {
					for(i++ ; i < args.length; i++) {
						installerArgs.add(args[i]);
					}
					break;
				} else if (args[i].equals("--help")) {
					printHelp(null, name);
					return;
				} else {
					throw new IllegalArgumentException("Invalid option.");
				}
			}
		} catch (Exception e) {
			printHelp(e.getMessage(), name);
		}

		var destDir = Files.createTempDirectory("frk");
		if (!quiet) {
		    if(verbose)
		        System.out.println(MessageFormat.format("Extracting {0} {1} to {2}", name, version, destDir));
		    else
                System.out.println(MessageFormat.format("Extracting {0} {1}", name, version));
		}
		int ret = 0;
		try {
			Files.createDirectories(destDir);
			int e = 0;
			try (var zis = new ZipInputStream(SelfExtractor.class.getResourceAsStream("/data.zip"))) {
				var zipEntry = zis.getNextEntry();
				while (zipEntry != null) {
					if (!quiet) {
						if (verbose)
							System.out.println("    " + zipEntry.getName());
						else {
							switch (e) {
							case 0:
								System.out.print("|");
								break;
							case 1:
								System.out.print("/");
								break;
							case 2:
								System.out.print("-");
								break;
							case 3:
								System.out.print("\\");
								break;
							case 4:
								System.out.print("|");
								break;
							case 5:
								System.out.print("/");
								break;
							case 6:
								System.out.print("-");
								break;
							case 7:
								System.out.print("\\");
								break;
							}
							System.out.print((char)8);
							e++;
							if (e > 7)
								e = 0;
						}
					}

					Boolean read = false;
					Boolean write = false;
					Boolean execute = false;
					String link = null;
					var extra = zipEntry.getExtra() == null ? "" : new String(zipEntry.getExtra(), "UTF-8");
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
						case 'L':
							var slen = Character.toString(extrac[++i]) + Character.toString(extrac[++i])
									+ Character.toString(extrac[++i]) + Character.toString(extrac[++i]);
							int len = Integer.valueOf(slen);
							var linkpath = new StringBuffer();
							for (int j = 0; j < len; j++) {
								linkpath.append(extrac[++i]);
							}
							link = linkpath.toString();
							break;
						}
					}

					var newFile = newFile(destDir, zipEntry);
					if (link != null) {
						Files.createSymbolicLink(newFile, Paths.get(link));
					} else if (zipEntry.isDirectory()) {
						if (!Files.isDirectory(newFile)) {
							Files.createDirectories(newFile);
						}
					} else {
						// fix for Windows-created archives
						var parent = newFile.getParent();
						if (!Files.isDirectory(parent)) {
							Files.createDirectories(parent);
						}

						// write file content
						try (OutputStream fos = new BufferedOutputStream(Files.newOutputStream(newFile))) {
							zis.transferTo(fos);
						}
					}

					if (read != null) {
						newFile.toFile().setReadable(read);
					}

					if (write != null) {
						newFile.toFile().setWritable(write);
					}

					if (execute != null) {
						newFile.toFile().setExecutable(execute);
					}

					zipEntry = zis.getNextEntry();
				}
				zis.closeEntry();
			}

			if (exec) {
				var startupScript = props.getProperty(EXEC);
				if (startupScript != null) {
					installerArgs.add(0, destDir.resolve(startupScript).toAbsolutePath().toString());
					if (!quiet) {
					    if(verbose)
					        System.out.println("Starting installer (" + String.join(" ", installerArgs) + ")");
					    else
					        System.out.println("Starting installer .. ");
					}
					var pb = new ProcessBuilder(installerArgs);
					pb.redirectError(Redirect.INHERIT);
					pb.redirectInput(Redirect.INHERIT);
					pb.redirectOutput(Redirect.INHERIT);
					pb.directory(destDir.toFile());
					Process p = pb.start();
					ret = p.waitFor();
				}
				else if(!quiet && verbose) {
				    System.out.println("This archive contains no installer script.");
				}
			}
		} finally {
			if (cleanup) {
				if (!quiet)
					System.out.println("Cleaning up " + destDir);
				try (Stream<Path> walk = Files.walk(destDir)) {
					walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
				}
			}
		}
		System.exit(ret);
	}

	static Path newFile(Path destinationDir, ZipEntry zipEntry) throws IOException {
		var destFile = destinationDir.resolve(zipEntry.getName());

		var destDirPath = destinationDir.normalize().toString();
		var destFilePath = destFile.normalize().toString();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}
}
