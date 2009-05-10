package de.haukerehfeld.quakeinjector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;


public class Installer {
	private static final int simultanousDownloads = 1;
	
	private ExecutorService pool;

	public Installer() {
		pool = Executors.newFixedThreadPool(simultanousDownloads);
	}

	public void install(final MapInfo selectedMap,
						final String url,
						final String installDirectory,
						final InstalledMaps installed) {
		final InstallMapInfo installer = new InstallMapInfo(selectedMap, url, installDirectory);

		pool.submit(installer);

		SwingWorker<Void,Void> saveInstalled = new SwingWorker<Void,Void>() {
			@Override
			public Void doInBackground() {
				MapFileList files;
				try {
					files = installer.get();
				}
				catch (java.lang.InterruptedException e) {
					throw new RuntimeException("Couldn't get installed file list!"
											   + e.getMessage());
				}
				catch (java.util.concurrent.ExecutionException e) {
					throw new RuntimeException("Couldn't get installed file list!"
											   + e.getMessage());
				}

				synchronized (installed) {
					installed.add(files);
					try {
						installed.write();
					}
					catch (java.io.IOException e) {
						System.out.println("Couldn't write installed Maps file!" + e.getMessage());
					}
				}
				return null;
			}
		};
		saveInstalled.execute();
		
	}

}