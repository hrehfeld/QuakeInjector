/*
Copyright 2009 Hauke Rehfeld


This file is part of QuakeInjector.

QuakeInjector is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

QuakeInjector is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with QuakeInjector.  If not, see <http://www.gnu.org/licenses/>.
*/

package de.haukerehfeld.quakeinjector;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.util.zip.CRC32;

public class Utils {
	public static final int BUFFERSIZE = 4096;

	public static <T> String join(final Iterable<T> objs, final String delimiter) {
		Iterator<T> iter = objs.iterator();
		if (!iter.hasNext())
			return "";
		StringBuffer buffer = new StringBuffer(String.valueOf(iter.next()));
		while (iter.hasNext()) {
			buffer.append(delimiter).append(String.valueOf(iter.next()));
		}
		return buffer.toString();
	}

	/**
	 * Like File.mkdirs() this creates all parent directories of f, but returns them in a list
	 */
	public static List<File> mkdirs(File f) {
		ArrayList<File> files = new ArrayList<File>();
		
		if (f.isDirectory()) {
			files.add(f);
		}

		File parentDir = f.getParentFile();
		while (parentDir != null && !parentDir.exists()) {
			files.add(parentDir);
			parentDir = parentDir.getParentFile();
		}

		java.util.Collections.reverse(files);

		for (File dir: files) {
			System.out.println("Creating dir " + dir);
			dir.mkdir();
		}

		return files;
	}

	/**
	 * Write stream to file in chunks of 2048 bytes
	 *
	 * doesn't do any checks.
	 */
	public static long writeFile(InputStream in, File file, ProgressListener progress)
		throws IOException {
		return writeFile(in, file, 2048, progress);
	}

	/**
	 * Write stream to file
	 * @return crc32 checksum
	 */
	public static long writeFile(final InputStream in,
	                       final File file,
	                       final int BUFFERSIZE,
	                       final ProgressListener progress) throws IOException {
		final BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(file),
		                                                           BUFFERSIZE);

		long crc = copy(in, dest, BUFFERSIZE, progress);
		dest.close();

		return crc;
	}

	public static long copy(final InputStream in,
	                        final OutputStream out,
	                        final int BUFFERSIZE,
	                        final ProgressListener progress) throws IOException {
		byte data[] = new byte[BUFFERSIZE];
		CRC32 crc = new CRC32();

		int readcount;
		while ((readcount = in.read(data, 0, BUFFERSIZE)) != -1) {
			if (progress != null) {
				progress.publish(readcount);
			}
			out.write(data, 0, readcount);
			crc.update(data, 0, readcount);
		}
		out.flush();
		return crc.getValue();
	}


	public static long getCrc32(final InputStream in, final ProgressListener progress) throws IOException {
		return copy(in, new NoOutputStream(), BUFFERSIZE, progress);
	}
	
	public static class NoOutputStream extends OutputStream {
		public void write(int b) {}
		public void write(byte[] b) {}
		public void write(byte[] b, int off, int len) {}
	}
}