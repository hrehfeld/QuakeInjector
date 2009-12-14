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

//import java.awt.*;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.haukerehfeld.quakeinjector.gui.ProgressPopup;
import de.haukerehfeld.quakeinjector.packagelist.model.PackageListModel;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class QuakeInjector extends JFrame {
	/**
	 * Window title
	 */
	private static final String applicationName = "Quake Injector";
	private static final int minWidth = 300;
	private static final int minHeight = 300;

	private EngineStarter starter;

	private PackageInteractionPanel interactionPanel;
	private RequirementList maps;
	private PackageList packages;
	private final PackageListModel maplist;
	private Installer installer;

	/**
	 * Thread worker to parse the installed maps in background
	 */
	private SwingWorker<List<PackageFileList>,Void> parseInstalled;


	public QuakeInjector() {
		super(applicationName);

		loadConfig.execute();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setLayout(new BoxLayout(getContentPane(),
								BoxLayout.PAGE_AXIS));

		maps = new RequirementList();
		packages = new PackageList(maps);
		maplist = new PackageListModel(packages);

		setJMenuBar(createMenuBar());
		
		setMinimumSize(new Dimension(minWidth, minHeight));
		
		addMainPane(getContentPane());

		addWindowListener(new QuakeInjectorWindowListener());

		setWindowSize();
	}

	private Menu createMenuBar() {

		ActionListener parseDatabase = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					parseInstalled();
					parseDatabaseAndSetList();
				}
			};

		ActionListener quit = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
						dispose();
					}
			};

		ActionListener showEngineConfig = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						showEngineConfig(maps.get("rogue").isInstalled(),
						                 maps.get("hipnotic").isInstalled());
					}};

		return new Menu(parseDatabase, quit, showEngineConfig);
	}

	/**
	 * Try setting the saved window size and position
	 */
	private void setWindowSize() {
		Configuration c = getConfig();

		if (c.MainWindowWidth.exists() && c.MainWindowHeight.exists()) {
			int width = c.MainWindowWidth.get();
			int height = c.MainWindowHeight.get();
			if (c.MainWindowPositionX.exists() && c.MainWindowPositionY.exists()) {
				int posX = c.MainWindowPositionX.get();
				int posY = c.MainWindowPositionY.get();
				System.out.println("Setting window bounds: "
				                   + posX + ", "
				                   + posY + ", "
				                   + width + ", "
				                   + height);
			
				setBounds(posX, posY, width, height);
			}
			else {
				System.out.println("Setting window size: " + width + ", " + height);
				setSize(width, height);
			}
		}
		else {
			pack();
		}
	}
		

	private void parseInstalled() {
		parseInstalled = new ParseInstalledWorker();
		parseInstalled.execute();
	}

	private void init() {
		parseInstalled();
		final Future<Void> requirementsListUpdater = parseDatabaseAndSetList();

		Configuration.EnginePath enginePathV = getConfig().EnginePath;
		File enginePath = enginePathV.get();
		File engineExe;
		if (getConfig().EngineExecutable.existsOrDefault()) {
			engineExe = new File(enginePathV.get()
			                          + File.separator
			                          + getConfig().EngineExecutable);
		}
		else {
			engineExe = new File("");
		}
		starter = new EngineStarter(enginePath,
		                            engineExe,
		                            getConfig().EngineCommandLine);
		installer = new Installer(enginePathV,
		                          getConfig().DownloadPath);
		interactionPanel.init(installer,
		                      getConfig().RepositoryBasePath,
		                      packages,
		                      starter);

		if (!installer.checkInstallDirectory()) {
			//wait until database was loaded, then pop up config
			new SwingWorker<Void,Void>() {
				@Override
			    public Void doInBackground() {
					try {
						requirementsListUpdater.get();
					}
					catch (java.lang.InterruptedException e) {}
					catch (java.util.concurrent.ExecutionException e) {}
					return null;
				}
				@Override
			    public void done() {
					enginePathNotSetDialogue();
				}
			}.execute();
		}
	}

	/**
	 * Thread worker to parse the config file in background
	 */
	private final SwingWorker<Configuration,Void> loadConfig
	    = new SwingWorker<Configuration,Void>() {
		@Override
		public Configuration doInBackground() {
			return new Configuration();
		}
	};

	private synchronized Configuration getConfig() {
		try {
			return loadConfig.get();
		}
		catch (InterruptedException e) {
			System.out.println(e);
			e.printStackTrace();
		}
		catch (java.util.concurrent.ExecutionException e) {
			System.out.println(e);
			e.printStackTrace();
		}
		throw new RuntimeException("Couldn't Load config!");
	}

	/**
	 * Parse the online database
	 */
	private Future<List<Requirement>> parseDatabase() {
		final PackageDatabaseParserWorker dbParse
		    = new PackageDatabaseParserWorker(getConfig().RepositoryDatabasePath.get());

		final ProgressPopup dbpopup =
		    new ProgressPopup("Downloading package database",
		                      new ActionListener() {
								  public void actionPerformed(ActionEvent e) {
									  dbParse.cancel(true);
								  }
							  },
		                      QuakeInjector.this);

		dbParse.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName() == "progress") {
						int p = (Integer) evt.getNewValue();
						dbpopup.setProgress(p);
					}
					else if (evt.getPropertyName() == "state"
					    && evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
						dbpopup.close();
					}
				}
			});
		dbParse.execute();
		dbpopup.pack();
		dbpopup.setVisible(true);

		return dbParse;
	}

	/**
	 * Tell maps what maps are already installed
	 */
	private void setInstalledStatus(final List<Requirement> dbParse,
	                                final List<PackageFileList> packages) {
		maps.setRequirements(dbParse);
		
		for (PackageFileList l: packages) {
			maps.setInstalled(l);
		}
		
		maps.notifyChangeListeners();
	}

	private Future<Void> parseDatabaseAndSetList() {
		final Future<List<Requirement>> dbParse = parseDatabase();

		SwingWorker<Void,Void> updateRequirementList = new SwingWorker<Void,Void>() {
			List<Requirement> database;
			List<PackageFileList> packages;
			
			@Override
			public Void doInBackground() throws ExecutionException, InterruptedException {
				database = dbParse.get();
				packages = parseInstalled.get();

				return null;
			}
			@Override
			public void done() {
				try {
					get();
				}
				catch (ExecutionException e) {
					Throwable err = e.getCause();

					String msg = "Database parsing failed! " + err.getMessage();
					JOptionPane.showMessageDialog(QuakeInjector.this,
					                              msg,
					                              "Database parsing failed!",
					                              JOptionPane.ERROR_MESSAGE);
				}
				catch (InterruptedException e) {
					System.err.println("Waiting for database parsing failed: " + e);
					e.printStackTrace();
				}

				if (database != null && packages != null) {
					setInstalledStatus(database, packages);
				}
			}
		};
		updateRequirementList.execute();
		
		return updateRequirementList;
	}


	/**
	 * Thread worker to parse the installed maps in background
	 */
	private static class ParseInstalledWorker extends SwingWorker<List<PackageFileList>, Void> {
		@Override
		public List<PackageFileList> doInBackground() {
			List<PackageFileList> files;
			try {
				files = new InstalledPackageList().read();
			}
			catch (java.io.FileNotFoundException e) {
				System.out.println("Notice: InstalledMaps xml doesn't exist yet,"
				                   + " no maps installed?");
				return new ArrayList<PackageFileList>();
			}
			catch (java.io.IOException e) {
				System.out.println("Error: InstalledMaps xml couldn't be loaded: "
				                   + e.getMessage());
				
				return new ArrayList<PackageFileList>();
			}
				
			return files;
		}
	}

	private List<PackageFileList> getInstalledFileLists() {
		try {
			synchronized (parseInstalled) {
				return parseInstalled.get();
			}
		}
		catch (InterruptedException e) { }
		catch (java.util.concurrent.ExecutionException e) {}
		return new ArrayList<PackageFileList>();
	}
	
	private void showEngineConfig(boolean rogueInstalled, boolean hipnoticInstalled) {
		final EngineConfigDialog d
		    = new EngineConfigDialog(QuakeInjector.this,
		                             getConfig().EnginePath,
		                             getConfig().EngineExecutable,
		                             getConfig().DownloadPath,
		                             getConfig().EngineCommandLine,
		                             rogueInstalled,
		                             hipnoticInstalled
		        );
		d.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
								saveEngineConfig(d.getEnginePath(),
								                 d.getEngineExecutable(),
								                 d.getDownloadPath(),
								                 d.getCommandline(),
								                 d.getRogueInstalled(),
								                 d.getHipnoticInstalled());
				}
			});
		
		d.packAndShow();
	}


	private void saveEngineConfig(File enginePath,
								  File engineExecutable,
	                              File downloadPath,
	                              String commandline,
	                              boolean rogueInstalled,
	                              boolean hipnoticInstalled) {
		Configuration c = getConfig();
		c.EnginePath.set(enginePath);
		c.EngineExecutable.set(RelativePath.getRelativePath(enginePath, engineExecutable));
		c.EngineCommandLine.set(commandline);

		c.DownloadPath.set(downloadPath);

		setEngineConfig(enginePath, engineExecutable, getConfig().EngineCommandLine, rogueInstalled, hipnoticInstalled);

		
		c.write();
	}

	private void setEngineConfig(File enginePath,
								 File engineExecutable,
	                             Configuration.EngineCommandLine commandline,
	                             boolean rogueInstalled,
	                             boolean hipnoticInstalled) {
		starter.setQuakeDirectory(enginePath);
		starter.setQuakeExecutable(engineExecutable);
		starter.setQuakeCommandline(commandline);

		maps.get("rogue").setInstalled(rogueInstalled);
		maps.get("hipnotic").setInstalled(hipnoticInstalled);
		try {
			synchronized (maps) {
				maps.writeInstalled();
			}
		}
		catch (java.io.IOException e) {}
	}

	private void addMainPane(Container panel) {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());

		
		//create a table
		final PackageTable table =  new PackageTable(maplist);
		maplist.size(table);

		{
			JPanel filterPanel = new JPanel();
			filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.LINE_AXIS));
			JLabel filterText = new JLabel("Filter: ", SwingConstants.TRAILING);
			filterPanel.add(filterText);

			final JTextField filter = new JTextField();
			filter.getDocument().addDocumentListener(
                new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) { filter(); }
                    public void insertUpdate(DocumentEvent e) { filter(); }
                    public void removeUpdate(DocumentEvent e) { filter(); }

					private void filter() {
						table.getRowSorter().setRowFilter(maplist.filter(filter.getText()));
					}
                });
			filterText.setLabelFor(filter);
			filterPanel.add(filter);

			mainPanel.add(filterPanel, new GridBagConstraints() {{
				anchor = LINE_START;
				fill = HORIZONTAL;
				weightx = 1;
				weighty = 0;
			}});

		}
		

		//Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);


		mainPanel.add(scrollPane, new GridBagConstraints() {{
				anchor = CENTER;
				fill = BOTH;
				gridx = 0;
				gridy = 1;
				gridwidth = 1;
				gridheight = 1;
				weightx = 1;
				weighty = 1;
			}});

		final InstallQueuePanel installQueue = new InstallQueuePanel();

		this.interactionPanel = new PackageInteractionPanel(this, installQueue);

		JPanel infoPanel = new JPanel(new GridBagLayout());

		PackageDetailPanel details = new PackageDetailPanel();
		infoPanel.add(details, new GridBagConstraints() {{
			anchor = PAGE_START;
			fill = BOTH;
			weightx = 1;
			weighty = 1;
		}});

		infoPanel.add(interactionPanel, new GridBagConstraints() {{
			gridy = 1;
			fill = BOTH;
			weightx = 1;
		}});

// 		JLabel queueLabel = new JLabel("Install Queue");
// 		infoPanel.add(queueLabel, new GridBagConstraints() {{
// 			anchor = PAGE_END;
// 			fill = BOTH;
// 			gridy = 2;
// 			weightx = 1;
// 		}});

		JScrollPane queueScroll = new JScrollPane(installQueue);
		infoPanel.add(queueScroll, new GridBagConstraints() {{
			anchor = PAGE_END;
			fill = BOTH;
			gridy = 3;
			weightx = 1;
			weighty = 1;
		}});

		JSplitPane infoSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
		                                      infoPanel,
		                                      queueScroll);
		infoSplit.setOneTouchExpandable(true);
		infoSplit.setResizeWeight(1);
		infoSplit.setContinuousLayout(true);
		infoSplit.setDividerLocation(400);
		infoSplit.setMinimumSize(new Dimension(200, 300));
		
		PackageListSelectionHandler selectionHandler
			= new PackageListSelectionHandler(maplist,
											  table);
		table.getSelectionModel().addListSelectionListener(selectionHandler);
		selectionHandler.addSelectionListener(interactionPanel);
		selectionHandler.addSelectionListener(details);


		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		                                      mainPanel,
		                                      infoSplit);
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(1);
		splitPane.setContinuousLayout(true);
		splitPane.setMinimumSize(new Dimension(450, 300));

		panel.add(splitPane);

		
	}

	
	private void display() {
		//pack();
		setVisible(true);
	}

	/**
	 * @return false if the user didn't open the config dialog
	 */
	public boolean enginePathNotSetDialogue() {
		String msg = "Quakepath isn't set correctly.\n"
		    + "It  needs to be set before trying to install (or play).";

		Object[] options = {"Open Engine Configuration",
		                    "Cancel"};
		int openEngineConfig =
		    JOptionPane.showOptionDialog(QuakeInjector.this,
		                                 msg,
		                                 "Quakepaths incorrect",
		                                 JOptionPane.YES_NO_OPTION,
		                                 JOptionPane.ERROR_MESSAGE,
		                                 null,
		                                 options,
		                                 options[0]);
		//button for engine config pressed
		if (openEngineConfig == 0) {
			//wait until maps are finished loading
			showEngineConfig(maps.get("rogue").isInstalled(),
			                 maps.get("hipnotic").isInstalled());
			return true;
		}
		else {
			return false;
		}
	}


	public static void main(String[] args) {
		try {
        // Set System L&F
			javax.swing.UIManager.setLookAndFeel(
				javax.swing.UIManager.getSystemLookAndFeelClassName());
		} 
		catch (javax.swing.UnsupportedLookAndFeelException e) {
		}
		catch (ClassNotFoundException e) {
		}
		catch (InstantiationException e) {
		}
		catch (IllegalAccessException e) {
		}

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					QuakeInjector qs = new QuakeInjector();
					qs.display();
					qs.init();
				}
			});

	}

	private class QuakeInjectorWindowListener extends WindowAdapter
	{
		@Override
		public void windowClosing(WindowEvent e) {
			windowClosed(e);
		}
		@Override
		public void windowClosed(WindowEvent e)
		{
			Configuration config = getConfig();
			Rectangle bounds = QuakeInjector.this.getBounds();
			config.MainWindowPositionX.set((int) bounds.getX());
			config.MainWindowPositionY.set((int) bounds.getY());
			config.MainWindowWidth.set((int) bounds.getWidth());
			config.MainWindowHeight.set((int) bounds.getHeight());
			config.write();
			System.out.println("Closing Window: " + (int) bounds.getWidth()
			    + (int) bounds.getHeight());

			System.exit(0);
		}

	}
	
}