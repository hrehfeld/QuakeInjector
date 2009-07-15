package de.haukerehfeld.quakeinjector;

import java.util.Iterator;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

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