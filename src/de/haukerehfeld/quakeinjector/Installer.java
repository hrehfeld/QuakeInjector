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
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.lang.reflect.InvocationTargetException;

import java.util.List;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.SwingWorker;
import javax.swing.SwingUtilities;

public class Installer {
	private static final int simultanousDownloads = 1;
	private static final int simultanousInstalls = 1;

	private Configuration.EnginePath installDirectory;
	private Configuration.DownloadPath downloadDirectory;
	
	private ExecutorService activeDownloaders = Executors.newFixedThreadPool(simultanousDownloads);
	private ExecutorService activeInstallers = Executors.newFixedThreadPool(simultanousInstalls);

	private Map<Package,Worker> queue = new HashMap<Package,Worker>();

	public Installer(Configuration.EnginePath installDirectory, Configuration.DownloadPath downloadDirectory) {
		this.installDirectory = installDirectory;
		this.downloadDirectory = downloadDirectory;
	}

	public boolean checkInstallDirectory() {
		if (!installDirectory.existsOrDefault()) {
			return false;
		}
		return installDirectory.get().canWrite();
	}

	public boolean checkDownloadDirectory() {
		if (!downloadDirectory.existsOrDefault()) {
			return false;
		}
		return downloadDirectory.get().canWrite();
	}
	
	public boolean alreadyQueued(final Package map) {
		return queue.get(map) != null;
	}

	public void install(final Package selectedMap,
	                    final String url,
						final InstallErrorHandler errorHandler,
						final PropertyChangeListener downloadProgressListener) {
		//map already in the instalation queue
		if (alreadyQueued(selectedMap)) {
			return;
		}
		
		//wait for the download to finish, then start
		//installation. after that, handle errors or save status
		Worker saveInstalled = new Worker(url,
		                                  selectedMap,
		                                  errorHandler,
		                                  downloadProgressListener);
		synchronized (queue) { queue.put(selectedMap, saveInstalled); }
		saveInstalled.execute();
		
	}

	public void cancel(Package installerMap) {
		Worker w;
		synchronized (queue) { w = queue.get(installerMap); }
		w.cancel();
	}


	public void uninstall(PackageFileList map,
	                      final UninstallErrorHandler errorHandler,
	                      PropertyChangeListener progressListener) {
		final UninstallWorker uninstall = new UninstallWorker(map, installDirectory.get().getAbsolutePath());
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

	public interface InstallErrorHandler {
		/**
		 * Decide which files in the package should be overwritten
		 * @param existingFiles in the package
		 * @return all the files that should be overwritten or an empty list to overwrite no files and cancel install
		 */
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
		public DownloadWorker downloader;

		private Throwable error;

		private final String url;
		private final Package map;
		private final InstallErrorHandler handler;
		private final PropertyChangeListener downloadProgressListener;

		public Worker(String url,
		              Package map,
		              InstallErrorHandler handler,
		              PropertyChangeListener downloadProgressListener) {
			this.url = url;
			this.map = map;
			this.handler = handler;
			this.downloadProgressListener = downloadProgressListener;
		}

		@Override
		    public Void doInBackground() {
			try {
				final File downloadFile = new File(downloadDirectory.get().getAbsolutePath() + File.separator + map.getId() + ".zip");
				System.out.println("Downloading to " + downloadFile);

				long downloadSize;
				if (!downloadFile.exists()) {
					downloadFile.getParentFile().mkdirs();
					FileOutputStream out = new FileOutputStream(downloadFile);
					//make sure file streams get closed
					try {
						downloadSize = download(url, out);
					}
					catch (Exception e) {
						throw e;
					}
					finally {
						out.flush();
						out.close();
					}
				}
				else {
					downloadSize = downloadFile.length();
					System.out.println("Skipping download, already existing with length " + downloadSize);
				}

				System.out.print("Inspecting downloaded archive...");
				FileInputStream in = new FileInputStream(downloadFile);
				Map<String,File> existingFiles = inspect(in);
				in.close();
				System.out.println("done.");

				List<File> overwrites = null;
				if (existingFiles != null) {
					overwrites = askForOverwrite(existingFiles);
				}

				System.out.println("After asking");

				if (overwrites == null || !overwrites.isEmpty()) {
					//and start install
					String mapDir = Installer.getUnzipDir(map, installDirectory.get().getAbsolutePath());
					in = new FileInputStream(downloadFile);
					installer = new InstallWorker(in,
					                              downloadSize,
					                              map,
					                              installDirectory.get(),
					                              mapDir,
					                              overwrites);
					synchronized (activeInstallers) { activeInstallers.submit(installer); }
					//make sure file streams get closed
					try {
						installer.get();
					}
					catch (Exception e) {
						throw e;
					}
					finally {
						in.close();
					}
				}
			}
			catch (java.io.FileNotFoundException e) {
				error = e;
			}
			catch (IOException e) {
				error = e;
			}
			catch (java.util.concurrent.ExecutionException e) {
				error = e.getCause();
			}
			catch (Exception e) {
				error = e;
				System.err.println("Caught 'unchecked' exception in Installer.Worker.doBackground(): " + e);
				e.printStackTrace();
			}
			return null;
		}

		private long download(String url,
		                     FileOutputStream out) throws
		    IOException,
		    InterruptedException,
			ExecutionException {
			//first, download the file 
			Download download = Download.create(url);

			downloader = new DownloadWorker(download, out);
			downloader.addPropertyChangeListener(downloadProgressListener);
			synchronized (activeDownloaders) { activeDownloaders.submit(downloader); }

			return downloader.get();
		}

		private Map<String,File> inspect(final InputStream in) throws
		    IOException,
		    InterruptedException,
			ExecutionException  {
			//see what files the zip wants to extract

			InspectZipWorker inspector = new InspectZipWorker(in);
			inspector.execute();

			final List<ZipEntry> entries = inspector.get();
			
			//check files
			final Map<String,File> files = new HashMap<String,File>();
			boolean existingFile = false;
			for (ZipEntry z: entries) {
				if (z.isDirectory()) {
					continue;
				}
				File f = getFile(z, map, installDirectory.get().getAbsolutePath());
				String name
				    = RelativePath.getRelativePath(installDirectory.get(), f).toString();
				files.put(name, f);
				if (f.exists()) {
					existingFile = true;
				}
			}
			if (!existingFile) {
				return null;
			}
			return files;
		}

		private List<File> askForOverwrite(final Map<String,File> files) throws
		    InterruptedException,
		    InvocationTargetException,
			ExecutionException {
			//popup overwrite dialog
			SwingWorker<List<File>,Void> dialogue = new SwingWorker<List<File>,Void>() {
				public List<File> doInBackground() {
					return handler.overwrite(files);
				}
			};
			SwingUtilities.invokeAndWait(dialogue);

			List<File> overwrites = dialogue.get();
			return overwrites;
		}
		
		public void cancel() {
			if (downloader != null) { downloader.cancel(true); }
			if (installer != null) { installer.cancel(true); }
			cancel(true);
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
				installer = null;
				downloader = null;
			}
			else if (isCancelled()) {
				System.out.println("CancelledException!");
				handler.handle(new CancelledException(), files);
			}
			else {
				handler.success(files);
			}

			System.out.println("Done saving installedmaps");
			synchronized (queue) { queue.remove(map); }

		}
	}	
}