package de.haukerehfeld.quakeinjector;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

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

	private JButton uninstallButton = new JButton(uninstallText);
	private JButton installButton = new JButton(installText);
	private JButton playButton = new JButton(playText);

	private JComboBox startmaps;
	
    /**
	 * Currently selected map
	 */
	private MapInfo selectedMap = null;

	private final Installer installer;
	
	public MapInfoPanel(String installDirectory,
						Paths paths,
						InstalledMaps installed,
						EngineStarter starter) {
		this.paths = paths;
		this.installDirectory = installDirectory;
		this.installed = installed;
		this.starter = starter;

		this.installer = new Installer();

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		
		uninstallButton = new JButton(uninstallText);
		uninstallButton.setEnabled(false);
		uninstallButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					uninstall();
				}
			});
		add(uninstallButton);

		installButton = new JButton(installText);
		installButton.setEnabled(false);
		installButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					install();
				}
			});
		add(installButton);

		playButton = new JButton(playText);
		playButton.setEnabled(false);
		playButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					start();
				}
			});
		add(playButton);

		startmaps = new JComboBox();
		add(startmaps);
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
				install(requirement);
			}
		}
	}

	public void install() {
		install(selectedMap);
	}

	public void install(MapInfo selectedMap) {
		installRequirements(selectedMap);
		
		installer.install(selectedMap,
						  paths.getRepositoryUrl(selectedMap.getId()),
						  installDirectory,
						  installed);

		installButton.setEnabled(false);


// 	@Override
//     public void done() {
// 		map.setInstalled(true);
		
// 	}
			
	}

	public void uninstall() {
		final MapFileList files;

		synchronized (installed) {
			files = installed.get(selectedMap.getId());
		}
		Uninstaller uninstall = new Uninstaller(selectedMap,
												files,
												installDirectory);
		uninstall.execute();
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

	public void stateChanged(ChangeEvent e) {
		refreshUi();
	}

	public void setInstallDirectory(String installDirectory) {
		this.installDirectory = installDirectory;
	}
}