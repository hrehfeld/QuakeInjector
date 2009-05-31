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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.util.List;

/**
 * the panel that shows Info about the selected map
 */
class PackageInteractionPanel extends JPanel implements ChangeListener,
											 PackageListSelectionHandler.SelectionListener {
	private static final String uninstallText = "Uninstall";
	private static final String installText = "Install";
	private static final String playText = "Play";

	private QuakeInjector main;
	
	private EngineStarter starter;
	private Paths paths;
	private PackageList requirements;
	private InstallQueuePanel installQueue;

	private JButton uninstallButton;
	private JButton installButton;
	private JButton playButton;

	private JComboBox startmaps;

	private boolean ready = false;

	/**
	 * Currently selected map
	 */
	private Package selectedMap = null;

	private Installer installer;
	
	public PackageInteractionPanel(QuakeInjector main, InstallQueuePanel installQueue) {
		super(new GridBagLayout());

		this.main = main;
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


		disableUI();
	}


	public void init(Installer installer,
	                 Paths paths,
	                 PackageList requirements,
	                 EngineStarter starter) {
		this.paths = paths;
		this.requirements = requirements;
		this.starter = starter;

		this.installer = installer;
		

		ready = true;
		refreshUi();
	}

	public void installRequirements(Package map) {
		for (Package requirement: map.getAvailableRequirements()) {
			String id = requirement.getId();
			
			if (requirement.isInstalled()) {
				System.out.print("Required package " + id + " already installed.");
			}
			else {
				System.out.print("Required package " + id + " not installed. Installing...");
				install(requirement, true);
			}
		}
	}

	private boolean hasCurrentPackage() {
		return (selectedMap != null);
	}
		

	public void install() {
		if (!hasCurrentPackage()) { return; }
		
		install(selectedMap, false);
	}

	private boolean checkInstallRequirements(Package selectedMap) {
		List<Requirement> unmet = selectedMap.getUnavailableRequirements();
		if (!unmet.isEmpty()) {
			String msg = "The following prerequisites to play "
			    + selectedMap.getId()
			    + " can't be installed automatically:\n"
			    + Utils.join(unmet, ",\n")
			    + ".\n";
			Object[] options = {"Install anyways",
			                    "Cancel Install"};
			int install =
			    JOptionPane.showOptionDialog(this,
			                                 msg,
			                                 "Prerequisites not available for automatic install",
			                                 JOptionPane.YES_NO_OPTION,
			                                 JOptionPane.WARNING_MESSAGE,
			                                 null,
			                                 options,
			                                 options[1]);
			if (install != 0) {
				return false;
			}
		}
		return true;
	}

	private boolean checkPlayRequirements(Package selectedMap) {
		List<Requirement> unmet = selectedMap.getUnmetRequirements();
		if (!unmet.isEmpty()) {
			String msg = "The following prerequisites to play "
			    + selectedMap.getId()
			    + " don't seem to be installed: \n"
			    + Utils.join(unmet, ",\n ")
			    + ".\nYou probably can't play this package.";
			Object[] options = {"Start anyways",
			                    "Cancel Start"};
			int install =
			    JOptionPane.showOptionDialog(this,
			                                 msg,
			                                 "Prerequisites not installed",
			                                 JOptionPane.YES_NO_OPTION,
			                                 JOptionPane.WARNING_MESSAGE,
			                                 null,
			                                 options,
			                                 options[1]);
			if (install != 0) {
				return false;
			}
		}
		return true;
	}

	private boolean checkInstallDirectory() {
		while (!installer.checkInstallDirectory()) {
			if (!main.enginePathNotSetDialogue()) {
				return false;
			}
		}
		return true;
	}
	
	public void install(final Package selectedMap, boolean becauseRequired) {
		if (!checkInstallDirectory()
		    || installer.alreadyInstalling(selectedMap)
		    || !checkInstallRequirements(selectedMap)) {
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
						  new Installer.InstallErrorHandler() {
							  public void handle(OnlineFileNotFoundException error) {
								  installQueue.finished(progressListener,
								                        "File not found");
								  
								  refreshUi();
								  String msg = "The file couldn't be found in the online"
								      + " repository";
								  JOptionPane.showMessageDialog(PackageInteractionPanel.this,
								                                msg,
								                                "File not found (404)",
								                                JOptionPane.WARNING_MESSAGE);
							  }
							  public void success(PackageFileList installedFiles) {
								  Requirement r = requirements.get(installedFiles.getId());
								  r.setInstalled(true);
								  ((Package) r).setFileList(installedFiles);

								  try {
									  requirements.writeInstalled();
								  }
								  catch (java.io.IOException e) {
									  System.out.println("Couldn't write installed Maps file!"
									                     + e.getMessage());
								  }
								  installQueue.finished(progressListener, "Success");
								  
							  }
							  public void handle(FileNotWritableException error,
							                     PackageFileList alreadyInstalledFiles) {
								  cleanup(alreadyInstalledFiles,
								          "Couldn't write");

								  String msg = "Couldn't write to harddisk! "
								      + error.getMessage();
								  JOptionPane.showMessageDialog(PackageInteractionPanel.this,
								                                msg,
								                                "Couldn't write to harddisk",
								                                JOptionPane.ERROR_MESSAGE);
							  }
							  public void handle(java.io.IOException error,
							                     PackageFileList alreadyInstalledFiles) {
								  cleanup(alreadyInstalledFiles, "Connection failed");

								  String msg = "Couldn't connect to the online repository! "
								      + error.getMessage();
								  JOptionPane.showMessageDialog(PackageInteractionPanel.this,
								                                msg,
								                                "Couldn't connect to repository",
								                                JOptionPane.ERROR_MESSAGE);
							  }
							  public void handle(Installer.CancelledException error,
							                     PackageFileList alreadyInstalledFiles) {
								  cleanup(alreadyInstalledFiles, "Canceled");
							  }

							  private void cleanup(PackageFileList alreadyInstalledFiles,
							                       String message) {
								  System.out.println("Cleaning up...");
								  uninstall(selectedMap, alreadyInstalledFiles);
								  installQueue.finished(progressListener, message);
							  }
						  },
						  progressListener);

		installButton.setEnabled(false);
	}

	public void uninstall() {
		if (!checkInstallDirectory()) {
			return;
		}
		if (!hasCurrentPackage()) { return; }

		uninstall(selectedMap, selectedMap.getFileList());
		uninstallButton.setEnabled(false);
	}

	private void uninstall(final Package map, PackageFileList files) {
		String description = "Uninstalling " + files.getId();
		
		final InstallQueuePanel.Job progressListener
		    = installQueue.addJob(description,
		                          new ActionListener() {
									  public void actionPerformed(ActionEvent e) {
//cancel button action
									  }
								  });

		installer.uninstall(files,
		                    new Installer.UninstallErrorHandler() {
								@Override
								public void success() {
									installQueue.finished(progressListener, "success");

									SwingWorker<Void,Void> saveInstalled
									    = new SwingWorker<Void,Void>() {
										@Override
										public Void doInBackground() {
											map.setInstalled(false);
											
											try {
												requirements.writeInstalled();
											}
											catch (java.io.IOException e) {
												System.out.println("Couldn't write installed Maps file!" + e.getMessage());
											}
											return null;
										}
									};
									saveInstalled.execute();
									

								}

								@Override
								public void error(Exception e) {
									installQueue.finished(progressListener, "fail");
									System.out.println(e.getMessage());
									e.printStackTrace();
								}
							},
		                    progressListener
		    );
	}

	public void start() {
		if (!hasCurrentPackage()) { return; }

		if (!starter.checkPaths()) {
			JOptionPane.showMessageDialog(main,
			                              "Quake engine paths aren't set correctly, can't start.",
			                              "Quake engine paths not configured",
			                              JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (!checkPlayRequirements(selectedMap)) {
			return;
		}
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

	public void setSelection(Package map) {
		this.selectedMap = map;

		refreshUi();

	}

	private void refreshUi() {
		if (!ready || !hasCurrentPackage()) {
			installButton.setText(installText);
			disableUI();
			return;
		}
		
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
			installButton.setEnabled(true);
			playButton.setEnabled(false);
			uninstallButton.setEnabled(false);
			startmaps.setEnabled(false);
		}

	}

	private void disableUI() {
		uninstallButton.setEnabled(false);
		playButton.setEnabled(false);
		installButton.setEnabled(false);
		startmaps.setEnabled(false);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		refreshUi();
	}

	@Override
	public void selectionChanged(Package s) {
		setSelection(s);
	}

}