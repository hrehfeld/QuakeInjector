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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.SwingWorker;

/**
 * Install maps in a worker thread
 * Init once and let swing start it - don't reuse
 */
public class InstallWorker extends SwingWorker<PackageFileList, Void> implements
																	  ProgressListener,
																	  Cancelable {
	private final static int BUFFERSIZE = 1024;
	
	private String baseDirectory;
	private String unzipDirectory;
	private Package map;
	private InputStream input;
	private List<File> overwrites;

	private long downloadSize = 0;
	private PackageFileList files;

	/**
	 * @param inputSize size of the input stream in bytes, for progress reporting
	 * @param overwrites a list of files that we should overwrite, or
	 * null if everything should be overwritten
	 */
	public InstallWorker(InputStream input,
	                     long inputSize,
	                     Package map,
	                     String baseDirectory,
	                     String unzipDirectory,
	                     List<File> overwrites) {
		this.map = map;
		this.input = input;
		this.downloadSize = inputSize;
		this.baseDirectory = baseDirectory;
		this.unzipDirectory = unzipDirectory;
		this.files = new PackageFileList(map.getId());
		this.overwrites = overwrites;
	}

	@Override
	public PackageFileList doInBackground() throws IOException,
	    FileNotFoundException,
	    Installer.CancelledException {
		System.out.println("Installing " + map.getId());

		unzip(input,
		      baseDirectory,
		      unzipDirectory,
		      map.getId(),
		      overwrites);
		
		map.setInstalled(true);
		return files;
	}


	/**
	 * Unzip from the inputstream to the quake base dir
	 *
	 * @return a map of temporary files that need to renamed to the entry files
	 */
	public void unzip(InputStream in,
	                            String basedir,
	                            String unzipdir,
	                            String mapid,
	                            List<File> overwrites)
	    throws IOException, FileNotFoundException, Installer.CancelledException {
		//build progress filter chain
		ProgressListener progress =
			    new SumProgressListener(
					new PercentageProgressListener(downloadSize,
					                               new CheckCanceledProgressListener(this,
					                                                                 this)));
		

		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(in));
		ZipEntry entry;

		boolean extracted = false;
		while((entry = zis.getNextEntry()) != null) {
			File f = Installer.getFile(entry, map, basedir);
			String filename = RelativePath.getRelativePath(new File(basedir), f).toString();
			
			if (overwrites != null && overwrites.indexOf(f) < 0) {
				System.out.println("Skipping " + filename + ", because it isn't supposed to be overwritten.");
				continue;
			}

			//create dirs
			List<File> createdDirs = Utils.mkdirs(f);
			for (File dirname: createdDirs) {
				//save relative paths to files so we can delete dirs later
				files.add(RelativePath.getRelativePath(new File(basedir), dirname).toString());
			}

			//do nothing for directories other than creating them
			if (entry.isDirectory()) {
				continue;
			}

			files.add(filename);
			System.out.println("Writing " + filename + " (" + entry.getCompressedSize() + "b)");

			File original = f;
			if (f.exists()) {
				//create Temp file and rename later
				f = f.createTempFile("quakeinjector", ".tmp", f.getParentFile());
				System.out.println("create Temp file " + f);
			}

			try {
				Utils.writeFile(zis,
				                f,
				                new CompressedProgressListener(entry.getCompressedSize()
				                                               / (double) entry.getSize(),
				                                               progress));
			}
			catch (FileNotFoundException e) {
				files.remove(filename);
				throw new FileNotWritableException(e.getMessage());
			}
			
			if (!f.equals(original)) {
				original.delete();
				System.out.println("moving Temp file to " + original);
				f.renameTo(original);
			}

			extracted = true;
		}

		if (!extracted) {
			throw new java.util.zip.ZipException("No files extracted from zip, is it an empty file?");
		}
		zis.close();
	}

	public void publish(long progress) {
		if (progress <= 100) {
			setProgress((int) progress);
		}
	}

	public void checkCancelled() throws Installer.CancelledException {
		if (isCancelled()) {
			System.out.println("canceling...");
			throw new Installer.CancelledException();
		}
	}

	public PackageFileList getInstalledFiles() {
		return files;
	}
}