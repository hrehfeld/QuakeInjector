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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.SwingWorker;

/**
 * Install maps in a worker thread
 * Init once and let swing start it - don't reuse
 */
public class InstallWorker extends SwingWorker<PackageFileList, Void> implements ProgressListener {
	private String url;
	private String baseDirectory;
	private Package map;

	private long downloadSize = 0;
	private long downloaded = 0;

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
			d.init();
			downloadSize = d.getSize();
			InputStream in = d.getStream();
			String relativedir = map.getRelativeBaseDir();
			String unzipdir = baseDirectory;
			if (relativedir != null) {
				unzipdir += File.separator + relativedir;
			}
		
			files = unzip(in, this.baseDirectory, unzipdir, map.getId());
		}
		catch (Installer.CancelledException e) {
			System.out.println("cancelled exception!");
			//throw e;
			throw new OnlineFileNotFoundException();
		}
		map.setInstalled(true);
		return files;
	}


	public PackageFileList unzip(InputStream in,
	                             String basedir,
	                             String unzipdir,
	                             String mapid)
	    throws IOException, FileNotFoundException, Installer.CancelledException {
		//build progress filter chain
		ProgressListener progress =
		      new SumProgressListener(
			    new PercentageProgressListener(downloadSize,
			      new CheckCanceledProgressListener(this,
			        this)));
		

		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(in));
		ZipEntry entry;


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
			
		}
		//save the mapfile list so we can uninstall
		zis.close();

		return files;
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