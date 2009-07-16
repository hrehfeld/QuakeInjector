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

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingWorker;

public class Installer {
	private static final int simultanousDownloads = 1;
	
	private String installDirectory;
	private ExecutorService pool;

	private Map<Package,InstallWorker> installers = new HashMap<Package,InstallWorker>();

	public Installer() {
		pool = Executors.newFixedThreadPool(simultanousDownloads);
	}

	public boolean alreadyInstalling(final Package map) {
		return installers.get(map) != null;
	}

	public void install(final Package selectedMap,
	                    final String url,
						final InstallErrorHandler errorHandler,
						final PropertyChangeListener propertyListener) {
		//map already in the instalation queue
		if (alreadyInstalling(selectedMap)) {
			return;
		}
		final InstallWorker installer = new InstallWorker(selectedMap, url, installDirectory);
		installers.put(selectedMap, installer);
		installer.addPropertyChangeListener(propertyListener);

		pool.submit(installer);

		SwingWorker<Void,Void> saveInstalled = new SwingWorker<Void,Void>() {
			private Throwable error;
			
			@Override
			public Void doInBackground() {
				try {
					installer.get();
				}
				catch (java.util.concurrent.ExecutionException e) {
					error = e.getCause();
				}
				catch (java.lang.InterruptedException e) {
					System.out.println("Interrupted: " + e);
				}

				return null;
			}

			@Override
			public void done() {
				PackageFileList files = installer.getInstalledFiles();
				
				if (error != null) {
					if (error instanceof OnlineFileNotFoundException) {
						errorHandler.handle((OnlineFileNotFoundException) error);
					}
					else if (error instanceof FileNotWritableException) {
						errorHandler.handle((FileNotWritableException) error, files);
					}
					else if (error instanceof IOException) {
						errorHandler.handle((IOException) error, files);
					}
					else {
						System.out.println("unhandled exception from install worker" + error);
						error.printStackTrace();
					}
				}
				else if (installer.isCancelled()) {
					System.out.println("CancelledException!");
					errorHandler.handle(new CancelledException(), files);
				}
				else {
					errorHandler.success(files);
				}

				System.out.println("Done saving installedmaps");
				installers.remove(selectedMap);
			}
		};
		saveInstalled.execute();
		
	}

	public void cancel(Package installerMap) {
		installers.get(installerMap).cancel(true);
	}

	public void setInstallDirectory(String installDirectory) {
		this.installDirectory = installDirectory;
	}

	public boolean checkInstallDirectory() {
		return new File(installDirectory).canWrite();
	}

	public void uninstall(PackageFileList map,
	                      final UninstallErrorHandler errorHandler,
	                      PropertyChangeListener progressListener) {
		final UninstallWorker uninstall = new UninstallWorker(map, installDirectory);
		uninstall.addPropertyChangeListener(progressListener);
		uninstall.execute();

		//wait until finished and set finished status
		new SwingWorker<Void,Void>() {
			@Override
			    public Void doInBackground() {
				try {
					uninstall.get();
					errorHandler.success();
				}
				catch (java.util.concurrent.ExecutionException e) {
					Throwable er = e.getCause();
					if (er instanceof Exception) {
						errorHandler.error((Exception) er);
					}
					else {
						errorHandler.error(e);
					}
				}
				catch (java.lang.InterruptedException e) {
					System.out.println("Installer: " + e.getMessage());
					e.printStackTrace();
				}
				return null;
			}
		}.execute();
	}

	public interface InstallErrorHandler {
		public void success(PackageFileList installedFiles);
		public void handle(OnlineFileNotFoundException error);
		public void handle(FileNotWritableException error, PackageFileList alreadyInstalledFiles);
		public void handle(IOException error, PackageFileList alreadyInstalledFiles);
		public void handle(CancelledException error, PackageFileList alreadyInstalledFiles);
	}

	public interface UninstallErrorHandler {
		public void success();
		public void error(Exception e);
	}

	public static class CancelledException extends RuntimeException {}

	
}