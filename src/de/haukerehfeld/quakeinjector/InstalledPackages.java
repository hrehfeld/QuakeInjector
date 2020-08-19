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

import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.List;
import java.util.Collections;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.SwingWorker;

public class InstalledPackages {
	private Future<List<PackageFileList>> installedPackages;

	public void parse(File installedPackagesXml) {
		ParseInstalledPackagesWorker parseInstalled
		    = new ParseInstalledPackagesWorker(installedPackagesXml);
		parseInstalled.execute();
		installedPackages = parseInstalled;
	}
	
	public List<PackageFileList> get() throws InterruptedException, ExecutionException {
		try {
			return installedPackages.get();
		}
		catch (InterruptedException | ExecutionException e) {
			throw e;
		}
	}
	

	public List<PackageFileList> getDefaultErrorHandling() {
		try {
			return get();
		}
		catch (InterruptedException e) {
			System.err.println("Error: couldn't set installedPackages, interrupted!" + e);
			e.printStackTrace();
		}
		catch (ExecutionException err) {
			try {
				throw err.getCause();
			}
			catch (java.io.FileNotFoundException e) {
				System.out.println("Notice: installed packages file doesn't exist yet,"
				                   + " no packages installed? " + e);
			}
			catch (java.io.IOException e) {
				System.err.println("Error: installed packages file couldn't be "
				                   + "loaded: " + e);
				e.printStackTrace();
			}
		}
		finally {
			return Collections.emptyList();
		}
	}


	class ParseInstalledPackagesWorker extends SwingWorker<List<PackageFileList>,Void> {
		public final File xml;

		public ParseInstalledPackagesWorker(File xml) {
			this.xml = xml;
		}
		
		@Override
		public List<PackageFileList> doInBackground() throws FileNotFoundException, IOException {
			return parseInstalledPackagesFile(xml);
		}
	}

	/**
	 * Thrown if no installed packages xml exists
	 */
	public static class NoInstalledPackagesFileException extends FileNotFoundException {
		public NoInstalledPackagesFileException(String msg) { super(msg); }
		public NoInstalledPackagesFileException() { super(); }
	}

	private List<PackageFileList> parseInstalledPackagesFile(File installedPackagesFile)
		throws NoInstalledPackagesFileException, FileNotFoundException, IOException {
		if (!installedPackagesFile.exists()) {
			throw new NoInstalledPackagesFileException("No Installed packages file (" + installedPackagesFile + ")"
			                                       + ", no packages installed?");
		}
		
		final InputStream in = new BufferedInputStream(new FileInputStream(installedPackagesFile));
		
		List<PackageFileList> result =  new InstalledPackageList().read(in);

		//close the stream after parsing is finished
		Exception error = null;
		try {
			in.close();
		} catch (IOException e) {
			error = e;
		}
		if (error != null ) {
			System.err.println("Error: couldn't close filestream " + installedPackagesFile + ": " + error);
		}
		return result;
	}
}