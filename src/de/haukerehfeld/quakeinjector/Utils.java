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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Utils {
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
	public static void writeFile(InputStream in, File file, ProgressListener progress)
		throws IOException {
		writeFile(in, file, 2048, progress);
	}

	/**
	 * Write stream to file
	 */
	public static void writeFile(final InputStream in,
	                       final File file,
	                       final int BUFFERSIZE,
	                       final ProgressListener progress) throws IOException {
		byte data[] = new byte[BUFFERSIZE];
		final BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(file),
		                                                           BUFFERSIZE);
		int readcount;
		while ((readcount = in.read(data, 0, BUFFERSIZE)) != -1) {
			progress.publish(readcount);
			dest.write(data, 0, readcount);
		}
		dest.flush();
		dest.close();
	}

	
	
}