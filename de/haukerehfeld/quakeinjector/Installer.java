package de.haukerehfeld.quakeinjector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import java.beans.PropertyChangeListener;

import java.io.IOException;

import java.util.Map;
import java.util.HashMap;

public class Installer {
	private static final int simultanousDownloads = 1;
	
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
						final String installDirectory,
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
				PackageFileList files;
				try {
					files = installer.get();
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
					System.out.println("exception from install worker");

					if (error instanceof OnlineFileNotFoundException) {
						errorHandler.handle((OnlineFileNotFoundException) error);
					}
					else if (error instanceof FileNotWritableException) {
						errorHandler.handle((FileNotWritableException) error, files);
					}
					else if (error instanceof IOException) {
						errorHandler.handle((IOException) error, files);
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

	public interface InstallErrorHandler {
		public void success(PackageFileList installedFiles);
		public void handle(OnlineFileNotFoundException error);
		public void handle(FileNotWritableException error, PackageFileList alreadyInstalledFiles);
		public void handle(IOException error, PackageFileList alreadyInstalledFiles);
		public void handle(CancelledException error, PackageFileList alreadyInstalledFiles);
	}

	public static class CancelledException extends Exception {}

}