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

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.sshtools.liftlib.ElevatedClosure;

public final class ElevatedHelpers {
	
	@SuppressWarnings("serial")
	public final static class FileExists implements ElevatedClosure<Boolean, Serializable> {

		private final String path;

		public FileExists(Path path) {
			this(path.toAbsolutePath().toString());
		}

		public FileExists(String path) {
			this.path = path;
		}

		@Override
		public Boolean call(ElevatedClosure<Boolean, Serializable> arg0) throws Exception {
			return Files.exists(Paths.get(path));
		}
	}

	@SuppressWarnings("serial")
	public final static class IsDirectory implements ElevatedClosure<Boolean, Serializable> {

		private final String path;

		public IsDirectory(Path path) {
			this(path.toAbsolutePath().toString());
		}

		public IsDirectory(String path) {
			this.path = path;
		}

		@Override
		public Boolean call(ElevatedClosure<Boolean, Serializable> arg0) throws Exception {
			return Files.isDirectory(Paths.get(path));
		}
	}

	@SuppressWarnings("serial")
	public final static class CreateDirectory implements ElevatedClosure<String[], Serializable> {

		private final String path;

		public CreateDirectory(Path path) {
			this(path.toAbsolutePath().toString());
		}

		public CreateDirectory(String path) {
			this.path = path;
		}

		@Override
		public String[] call(ElevatedClosure<String[], Serializable> arg0) throws Exception {
			var file = Paths.get(path);
			var created =new ArrayList<String>();
			var parent = file;
			while(parent != null && !Files.exists(file)) {
				created.add(file.toAbsolutePath().toString());
				parent = parent.getParent();
			}
			if(created.size() > 0)
				Files.createDirectories(file);
			return created.toArray(new String[0]);
		}

	}
	
	@SuppressWarnings("serial")
	public final static class DeleteFile implements ElevatedClosure<Boolean, Serializable> {

		private final String path;

		public DeleteFile(Path path) {
			this(path.toAbsolutePath().toString());
		}

		public DeleteFile(String path) {
			this.path = path;
		}

		@Override
		public Boolean call(ElevatedClosure<Boolean, Serializable> arg0) throws Exception {
			var file = Paths.get(path);
			var exists = Files.exists(file);
			Files.delete(file);
			return exists;
		}

	}

}
