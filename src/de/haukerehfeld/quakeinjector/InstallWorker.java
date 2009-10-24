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
	
	private String url;
	private String baseDirectory;
	private Package map;

	private long downloadSize = 0;
	private PackageFileList files;

	public InstallWorker(Package map,
					 String url,
					 String baseDirectory) {
		this.map = map;
		this.url = url;
		this.baseDirectory = baseDirectory;
		this.files = new PackageFileList(map.getId());

	}

	@Override
	public PackageFileList doInBackground() throws IOException,
	    FileNotFoundException,
	    Installer.CancelledException {
		System.out.println("Installing " + map.getId());

		try {
			Download d = Download.create(url);
			downloadSize = d.getSize();
			InputStream in = d.getStream();

			//build progress filter chain
			ProgressListener progress =
			    new SumProgressListener(
					new PercentageProgressListener(downloadSize,
					                               new CheckCanceledProgressListener(this,
					                                                                 this)));
			
			
			ByteArrayOutputStream temp = new ByteArrayOutputStream();

			{
				byte data[] = new byte[BUFFERSIZE];
				int readcount;
				while ((readcount = in.read(data, 0, BUFFERSIZE)) != -1) {
					progress.publish(readcount);
					temp.write(data, 0, readcount);
				}
			}			

			String relativedir = map.getRelativeBaseDir();
			String unzipdir = baseDirectory;
			if (relativedir != null) {
				unzipdir += File.separator + relativedir;
			}
		
			Map<File,File> filesToRename = unzip(in, this.baseDirectory, unzipdir, map.getId());
		}
		catch (Installer.CancelledException e) {
			System.out.println("cancelled exception!");
			//throw e;
			throw new OnlineFileNotFoundException();
		}

		map.setInstalled(true);
		return files;
	}


	/**
	 * Unzip from the inputstream to the quake base dir
	 *
	 * @return a map of temporary files that need to renamed to the entry files
	 */
	public Map<File,File> unzip(InputStream in,
	                             String basedir,
	                             String unzipdir,
	                             String mapid)
	    throws IOException, FileNotFoundException, Installer.CancelledException {
		

		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(in));
		ZipEntry entry;

		Map<File,File> fileRenames = new HashMap<File,File>();

		while((entry = zis.getNextEntry()) != null) {
			File f = new File(unzipdir + File.separator + entry.getName());

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

			String filename = RelativePath.getRelativePath(new File(basedir), f).toString();
			files.add(filename);
			System.out.println("Writing " + filename + " (" + entry.getCompressedSize() + "b)");

			if (f.exists()) {
				//create Temp file and rename later
				File temporaryFile = f.createTempFile("quakeinjector", "tmp");
				fileRenames.put(temporaryFile, f);
				System.out.println("Output file " + f
				                   + " already exists, writing to " + temporaryFile);
				f = temporaryFile;
			}

			// try {
				// Utils.writeFile(zis,
				//                 f,
				//                 new CompressedProgressListener(entry.getCompressedSize()
				//                                                / (double) entry.getSize(),
				//                                                progress));
			// }
			// catch (FileNotFoundException e) {
			// 	files.remove(filename);
			// 	throw new FileNotWritableException(e.getMessage());
			// }
			
		}
		zis.close();

		return fileRenames;
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