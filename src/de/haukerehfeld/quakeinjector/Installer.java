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
	private static final int simultanousInstalls = 1;
	
	private String installDirectory;
	private ExecutorService activeDownloaders = Executors.newFixedThreadPool(simultanousDownloads);
	private ExecutorService activeInstallers = Executors.newFixedThreadPool(simultanousInstalls);

	private Map<Package,InstallWorker> installerQueue = new HashMap<Package,InstallWorker>();
	private Map<Package,DownloadWorker> downloaderQueue = new HashMap<Package,DownloadWorker>();

	public boolean alreadyQueued(final Package map) {
		return installerQueue.get(map) != null || downloaderQueue.get(map) != null;
	}

	public void install(final Package selectedMap,
	                    final String url,
						final InstallErrorHandler errorHandler,
						final PropertyChangeListener downloadProgressListener) {
		//map already in the instalation queue
		if (alreadyQueued(selectedMap)) {
			return;
		}
		
		Download download;
		try {
			download = Download.create(url);
		}
		catch (IOException e) {
			errorHandler.handle((OnlineFileNotFoundException) e);
			return;
		}
		final long downloadSize = download.getSize();

		//first, download the file into memory
		final DownloadWorker downloader = new DownloadWorker(download);

		downloader.addPropertyChangeListener(downloadProgressListener);
		downloaderQueue.put(selectedMap, downloader);
		
		activeDownloaders.submit(downloader);

		//wait for the download to finish, then start
		//installation. after that, handle errors or save status
		SwingWorker<Void,Void> saveInstalled = new SwingWorker<Void,Void>() {
			public InstallWorker installer;
			private Throwable error;
			
			@Override
			public Void doInBackground() {
				try {
					//wait for download and start install
					installer = new InstallWorker(downloader.get(),
					                              downloadSize,
					                              selectedMap,
					                              installDirectory);
					synchronized (installerQueue) { installerQueue.put(selectedMap, installer); }
					synchronized (activeInstallers) { activeInstallers.submit(installer); }
					//wait for install
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
				//see if there was an error
				if (error != null) {
					try {
						throw error;
					}
					catch (OnlineFileNotFoundException error) {
						errorHandler.handle((OnlineFileNotFoundException) error);
					}
					catch (FileNotWritableException error) {
						errorHandler.handle((FileNotWritableException) error, files);
					}
					catch (IOException error) {
						errorHandler.handle((IOException) error, files);
					}
					catch (Throwable e) {
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
				synchronized (installerQueue) { installerQueue.remove(selectedMap); }
				synchronized (downloaderQueue) { downloaderQueue.remove(selectedMap); }

			}
		};
		saveInstalled.execute();
		
	}

	public void cancel(Package installerMap) {
		downloaderQueue.get(installerMap).cancel(true);
		installerQueue.get(installerMap).cancel(true);
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