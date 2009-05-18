package de.haukerehfeld.quakeinjector;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
 * the panel that shows Info about the selected map
 */
class MapInfoPanel extends JPanel implements ChangeListener {
	private static final String uninstallText = "Uninstall";
	private static final String installText = "Install";
	private static final String playText = "Play";
	
	private EngineStarter starter;
	private String installDirectory;
	private final Paths paths;
	private InstalledMaps installed;
	private InstallQueuePanel installQueue;

	private JButton uninstallButton;
	private JButton installButton;
	private JButton playButton;

	private JComboBox startmaps;

    /**
	 * Currently selected map
	 */
	private MapInfo selectedMap = null;

	private final Installer installer;
	
	public MapInfoPanel(String installDirectory,
						Paths paths,
						InstalledMaps installed,
						EngineStarter starter,
						InstallQueuePanel installQueue) {
		super(new GridBagLayout());
		this.paths = paths;
		this.installDirectory = installDirectory;
		this.installed = installed;
		this.starter = starter;

		this.installer = new Installer();
		this.installQueue = installQueue;

		uninstallButton = new JButton(uninstallText);
		uninstallButton.setEnabled(false);
		uninstallButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					uninstall();
				}
			});

		add(uninstallButton, new GridBagConstraints() {{
			fill = HORIZONTAL;
		}});

		installButton = new JButton(installText);
		installButton.setEnabled(false);
		int preferredHeight = (int) installButton.getPreferredSize().getHeight();
		{
			Dimension maxSize = new Dimension(150, preferredHeight);
			installButton.setMinimumSize(maxSize);
			installButton.setPreferredSize(maxSize);
		}
		installButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					install();
				}
			});
		add(installButton, new GridBagConstraints() {{
			gridx = 1;
			gridy = 0;
			fill = HORIZONTAL;
		}});

		playButton = new JButton(playText);
		playButton.setEnabled(false);
		playButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					start();
				}
			});
		add(playButton, new GridBagConstraints() {{
			gridx = 0;
			gridy = 1;
			fill = HORIZONTAL;
		}});

		startmaps = new JComboBox();
		{
			Dimension maxSize = new Dimension(100, preferredHeight);
			startmaps.setPreferredSize(maxSize);
			startmaps.setMinimumSize(maxSize);
		}
		add(startmaps, new GridBagConstraints() {{
			gridx = 1;
			gridy = 1;
			fill = HORIZONTAL;
			weightx = 1;
		}});

		
	}


	public void installRequirements(MapInfo map) {
		for (MapInfo requirement: map.getRequirements()) {
			String id = requirement.getId();
			
			MapFileList isInstalled;
			synchronized (installed) {
				isInstalled = installed.get(id);
			}

			if (isInstalled != null) {
					System.out.print("Required package " + id + " already installed.");
			}
			else {
				System.out.print("Required package " + id + " not installed. Installing...");
				install(requirement, true);
			}
		}
	}

	public void install() {
		install(selectedMap, false);
	}

	public void install(final MapInfo selectedMap, boolean becauseRequired) {
		if (installer.alreadyInstalling(selectedMap)) {
			return;
		}
		installRequirements(selectedMap);


		String description = "Installing ";
		if (becauseRequired) {
			description += "prerequisite ";
		}
		description += selectedMap.getId();
		
		final InstallQueuePanel.Job progressListener
			= installQueue.addJob(description,
								  new ActionListener() {
									  public void actionPerformed(ActionEvent e) {
										  installer.cancel(selectedMap);
									  }
								  });
		

		installer.install(selectedMap,
						  paths.getRepositoryUrl(selectedMap.getId()),
						  installDirectory,
						  new Installer.InstallErrorHandler() {
							  public void success(MapFileList installedFiles) {
								  synchronized (installed) {
									  installed.add(installedFiles);
									  try {
										  installed.write();
									  }
									  catch (java.io.IOException e) {
										  System.out.println("Couldn't write installed Maps file!"
															 + e.getMessage());
									  }
								  }

								  installQueue.finished(progressListener);
								  
							  }
							  public void handle(OnlineFileNotFoundException error) {
							  }
							  public void handle(FileNotWritableException error,
												 MapFileList alreadyInstalledFiles) {
								  System.out.println("Cleaning up...");
								  uninstall(alreadyInstalledFiles);
								  installQueue.finished(progressListener);
							  }
							  public void handle(java.io.IOException error,
												 MapFileList alreadyInstalledFiles) {
								  System.out.println("Cleaning up...");
								  uninstall(alreadyInstalledFiles);
								  installQueue.finished(progressListener);
							  }
							  public void handle(Installer.CancelledException error,
												 MapFileList alreadyInstalledFiles) {
								  System.out.println("Cleaning up...");
								  uninstall(alreadyInstalledFiles);
								  installQueue.finished(progressListener);
							  }
						  },
						  progressListener);

		installButton.setEnabled(false);
	}

	public void uninstall() {
		final MapFileList files;

		synchronized (installed) {
			files = installed.get(selectedMap.getId());
		}

		uninstall(files);
		uninstallButton.setEnabled(false);

		SwingWorker<Void,Void> saveInstalled = new SwingWorker<Void,Void>() {
			@Override
			public Void doInBackground() {
				synchronized (installed) {
					installed.remove(files);

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

	private void uninstall(MapFileList files) {
		Uninstaller uninstall = new Uninstaller(selectedMap,
												files,
												installDirectory);
		uninstall.execute();
	}

	public void start() {
		String startmap = (String) startmaps.getSelectedItem();
		System.out.println("startmap: " + startmap);

		try {
			starter.start(selectedMap.getCommandline(), startmap);
		}
		catch (java.io.IOException e) {
			/** @todo 2009-05-04 14:28 hrehfeld    pop up dialogue */
			System.out.println("Couldn't start quake engine: " + e.getMessage());
		}
	}

	public void setMapInfo(MapInfo map) {
		this.selectedMap = map;

		refreshUi();

	}

	private void refreshUi() {
		installButton.setText(installText + " " + selectedMap.getId());

		//we do this regardless of displaying the list, because we can
		//then simply get the selection from the list even if there's
		//only one option
		java.util.List<String> maps = selectedMap.getStartmaps();
		startmaps.removeAllItems();
		for (String startmap: maps) {
			startmaps.addItem(startmap);
		}

		if (selectedMap.isInstalled()) {
			installButton.setEnabled(false);
			uninstallButton.setEnabled(true);
			playButton.setEnabled(true);
			
			boolean enableList = false;
			if (maps.size() > 1) {
				enableList = true;
			}
			startmaps.setEnabled(enableList);
		}
		else {
			uninstallButton.setEnabled(false);
			playButton.setEnabled(false);
			installButton.setEnabled(true);
			startmaps.setEnabled(false);
		}

	}

	@Override
	public void stateChanged(ChangeEvent e) {
		refreshUi();
	}

	public void setInstallDirectory(String installDirectory) {
		this.installDirectory = installDirectory;
	}
}