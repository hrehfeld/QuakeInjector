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

import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.SwingWorker;
import javax.swing.SwingUtilities;

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
		SwingWorker<Void,Void> saveInstalled = new Worker(downloader,
		                                                  selectedMap,
		                                                  errorHandler,
		                                                  downloadSize);
		saveInstalled.execute();
		
	}

	public void cancel(Package installerMap) {
		DownloadWorker downloader = downloaderQueue.get(installerMap);
		if (downloader != null) {
			downloader.cancel(true);
		}
		InstallWorker installer = installerQueue.get(installerMap);
		if (installer != null) {
			installer.cancel(true);
		}
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

	public static String getUnzipDir(Package map, String baseDirectory) {
		String relativedir = map.getRelativeBaseDir();
		String unzipdir = baseDirectory;
		if (relativedir != null) {
			unzipdir += File.separator + relativedir;
		}
		return unzipdir;
	}

	public static File getFile(ZipEntry entry, Package map, String baseDirectory) {
		return new File(Installer.getUnzipDir(map, baseDirectory) + File.separator + entry.getName());
	}


	private static class InstallWorkers {
		public DownloadWorker downloader;
		public InstallWorker installer;
		public Worker worker;
	}

	public interface InstallErrorHandler {
		public List<File> overwrite(Map<String,File> existingFiles);
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

	private class Worker extends SwingWorker<Void,Void> {
		public InstallWorker installer;
		private Throwable error;

		private DownloadWorker downloader;
		private Package map;
		private InstallErrorHandler handler;
		private long downloadSize;

		public Worker(DownloadWorker downloader,
		              Package map,
		              InstallErrorHandler handler,
		              long downloadSize) {
			this.downloader = downloader;
			this.map = map;
			this.handler = handler;
			this.downloadSize = downloadSize;
		}

		@Override
		    public Void doInBackground() {
			try {
				//wait for download
				InputStream in = downloader.get();

				//see if we can reset the stream so that we can inspect it before extracting it
				boolean inspect = true;
				try {
					in.reset();
				}
				catch (IOException e) {
					inspect = false;
				}

				String mapDir = getUnzipDir(map, installDirectory);

				List<File> overwrites = null;
				if (inspect) {
					//see what files the zip wants to extract
					InspectZipWorker inspector = new InspectZipWorker(in);
					inspector.execute();
					final List<ZipEntry> entries = inspector.get();
					
					//check files
					final Map<String,File> files = new HashMap<String,File>();
					boolean overwrite = false;
					for (ZipEntry z: entries) {
						File f = getFile(z, map, installDirectory);
						String name
						    = RelativePath.getRelativePath(new File(installDirectory), f)
						    .toString();
						files.put(name, f);
						if (f.exists()) {
							overwrite = true;
						}
					}

					if (overwrite) {
						try {
							//popup overwrite dialog
							class StartOverwriteDialogue implements Runnable {
								public List<File> overwrites;
								
								public void run() {
									overwrites = handler.overwrite(files);
								}
							};
							StartOverwriteDialogue dialogue = new StartOverwriteDialogue();
							SwingUtilities.invokeAndWait(dialogue);
							synchronized (dialogue) {
								overwrites = dialogue.overwrites;
							}
						}
						catch (java.lang.reflect.InvocationTargetException e) {
							System.err.println("Couldn't call errorhandler to ask for"
							                   + " overwriting files");
							e.printStackTrace();
						}

						if (overwrites == null || overwrites.isEmpty()) {
							cancel(true);
							return null;
						}

						
					}

					try {
						in.reset();
					}
					catch (IOException e) {
						throw new RuntimeException("Can't reset stream although it worked"
						                           + " before!");
					}
				}

				//and start install
				installer = new InstallWorker(in,
				                              downloadSize,
				                              map,
				                              installDirectory,
				                              mapDir,
				                              overwrites);
				
				synchronized (installerQueue) { installerQueue.put(map, installer); }
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
			PackageFileList files;
			if (installer != null) {
				files = installer.getInstalledFiles();
			}
			else {
				files = new PackageFileList(map.getId());
			}
			
			//see if there was an error
			if (error != null) {
				try {
					throw error;
				}
				catch (OnlineFileNotFoundException error) {
					handler.handle((OnlineFileNotFoundException) error);
				}
				catch (FileNotWritableException error) {
					handler.handle((FileNotWritableException) error, files);
				}
				catch (IOException error) {
					handler.handle((IOException) error, files);
				}
				catch (Throwable e) {
					System.out.println("unhandled exception from install worker" + error);
					error.printStackTrace();
				}
			}
			else if (isCancelled() || downloader.isCancelled() || installer.isCancelled()) {
				System.out.println("CancelledException!");
				handler.handle(new CancelledException(), files);
			}
			else {
				handler.success(files);
			}

			System.out.println("Done saving installedmaps");
			synchronized (installerQueue) { installerQueue.remove(map); }
			synchronized (downloaderQueue) { downloaderQueue.remove(map); }

		}
	}	
}