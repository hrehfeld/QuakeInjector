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

	private Map<MapInfo,InstallMapInfo> installers = new HashMap<MapInfo,InstallMapInfo>();

	public Installer() {
		pool = Executors.newFixedThreadPool(simultanousDownloads);
	}

	public void install(final MapInfo selectedMap,
						final String url,
						final String installDirectory,
						final InstalledMaps installed,
						final InstallErrorHandler errorHandler,
						final PropertyChangeListener propertyListener) {
		final InstallMapInfo installer = new InstallMapInfo(selectedMap, url, installDirectory);
		installers.put(selectedMap, installer);
		installer.addPropertyChangeListener(propertyListener);

		pool.submit(installer);

		SwingWorker<Void,Void> saveInstalled = new SwingWorker<Void,Void>() {
			private Throwable error;
			
			@Override
			public Void doInBackground() {
				MapFileList files;
				try {
					files = installer.get();

					synchronized (installed) {
						installed.add(files);
						try {
							installed.write();
						}
						catch (java.io.IOException e) {
							System.out.println("Couldn't write installed Maps file!" + e.getMessage());
						}
					}
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
				MapFileList files = installer.getInstalledFiles();
				
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

				System.out.println("Done saving installedmaps");
				installers.remove(selectedMap);
			}
		};
		saveInstalled.execute();
		
	}

	public void cancel(MapInfo installerMap) {
		installers.get(installerMap).cancel(true);
	}

	public interface InstallErrorHandler {
		public void handle(OnlineFileNotFoundException error);
		public void handle(FileNotWritableException error, MapFileList alreadyInstalledFiles);
		public void handle(IOException error, MapFileList alreadyInstalledFiles);
		public void handle(CancelledException error, MapFileList alreadyInstalledFiles);
	}

	public static class CancelledException extends Exception {}

}