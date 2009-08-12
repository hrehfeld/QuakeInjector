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
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;


public class QuakeInjector extends JFrame {
	/**
	 * Window title
	 */
	private static final String applicationName = "Quake Injector";
	private static final int minWidth = 300;
	private static final int minHeight = 300;

	private Paths paths;

	private EngineStarter starter;

	private PackageInteractionPanel interactionPanel;
	private RequirementList maps;
	private PackageList packages;
	private final PackageListModel maplist;
	private Installer installer;



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
					parseDatabase();
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
		
		if (c.hasMainWindowSettings()) {
			int posX = c.getMainWindowPositionX();
			int posY = c.getMainWindowPositionY();
			int width = c.getMainWindowWidth();
			int height = c.getMainWindowHeight();
			
			System.out.println("Setting window size: "
			                   + posX + ", "
			                   + posY + ", "
			                   + width + ", "
			                   + height);
			
			setBounds(posX, posY, width, height);
			setSize(width, height);
		}
		else {
			pack();
		}
	}
		
	
	private void init() {
		parseInstalled.execute();

		final PackageDatabaseParserWorker dbParse = parseDatabase();


		paths = new Paths(getConfig().get("repositoryBase"));
		
		File enginePath = new File(getConfig().getEnginePath());
		File engineExe = new File(getConfig().getEnginePath()
		                          + File.separator
		                          + getConfig().getEngineExecutable());
		starter = new EngineStarter(enginePath,
		                            engineExe,
									getConfig().getEngineCommandline());
		installer = new Installer();
		interactionPanel.init(installer,
		                      paths,
		                      packages,
		                      starter);

		installer.setInstallDirectory(getConfig().getEnginePath());

		if (!installer.checkInstallDirectory()) {
			new SwingWorker<Void,Void>() {
				@Override
			    public Void doInBackground() {
					try {
						dbParse.get();
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
		catch (InterruptedException e) { }
		catch (java.util.concurrent.ExecutionException e) {}
		return null;
	}

	private PackageDatabaseParserWorker parseDatabase() {
		final PackageDatabaseParserWorker dbParse
		    = new PackageDatabaseParserWorker(maps,
		                                      getConfig().getRepositoryDatabase(),
		                                      new ThreadedGetter<List<PackageFileList>>() {
		                                          @Override
		                                          public List<PackageFileList> get()
		                                          throws java.util.concurrent.ExecutionException,
		                                          java.lang.InterruptedException {
													  return parseInstalled.get();
												  }
		                                      });
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
	 * Thread worker to parse the installed maps in background
	 */
	private final SwingWorker<List<PackageFileList>,Void> parseInstalled
	    = new SwingWorker<List<PackageFileList>, Void>() {
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

			
	};

	private synchronized List<PackageFileList> getInstalledFileLists() {
		try {
			return parseInstalled.get();
		}
		catch (InterruptedException e) { }
		catch (java.util.concurrent.ExecutionException e) {}
		return new ArrayList<PackageFileList>();
	}
	
	private void showEngineConfig(boolean rogueInstalled, boolean hipnoticInstalled) {
		final EngineConfigDialog d
		    = new EngineConfigDialog(QuakeInjector.this,
		                             getConfig().getEnginePath(),
		                             getConfig().getEngineExecutable(),
		                             getConfig().getEngineCommandline(),
		                             rogueInstalled,
		                             hipnoticInstalled
		        );
		d.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
								saveEngineConfig(d.getEnginePath(),
								                 d.getEngineExecutable(),
								                 d.getCommandline(),
								                 d.getRogueInstalled(),
								                 d.getHipnoticInstalled());
				}
			});
		
		d.packAndShow();
	}


	private void saveEngineConfig(File enginePath,
								  File engineExecutable,
	                              String commandline,
	                              boolean rogueInstalled,
	                              boolean hipnoticInstalled) {
		setEngineConfig(enginePath, engineExecutable, commandline,
		                rogueInstalled, hipnoticInstalled);

		getConfig().setEnginePath(enginePath.getAbsolutePath());
		getConfig().setEngineExecutable(RelativePath.getRelativePath(enginePath, engineExecutable).toString());
		getConfig().setEngineCommandline(commandline);
		
		getConfig().write();
	}

	private void setEngineConfig(File enginePath,
								 File engineExecutable,
	                             String commandline,
	                             boolean rogueInstalled,
	                             boolean hipnoticInstalled) {
		starter.setQuakeDirectory(enginePath);
		starter.setQuakeExecutable(engineExecutable);
		starter.setQuakeCommandline(commandline);

		maps.get("rogue").setInstalled(rogueInstalled);
		maps.get("hipnotic").setInstalled(hipnoticInstalled);
		try {
			maps.writeInstalled();
		}
		catch (java.io.IOException e) {}
		
		installer.setInstallDirectory(enginePath.getAbsolutePath());
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
		maplist.addChangeListener(interactionPanel);

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
		}});

		
		PackageListSelectionHandler selectionHandler
			= new PackageListSelectionHandler(maplist,
											  table);
		table.getSelectionModel().addListSelectionListener(selectionHandler);
		selectionHandler.addSelectionListener(interactionPanel);
		selectionHandler.addSelectionListener(details);


		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		                                      mainPanel,
		                                      infoPanel);
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

// 	/**
// 	 * Parse database xml and est in background
// 	 */
// 	public class DatabaseParser extends SwingWorker<List<Package>, Void>
// 		implements ProgressListener {
// 		private final PackageList requirementList;
// 		private final PackageListModel model;
		
// 		public DatabaseParser(final PackageList requirementList,
// 		                      final PackageListModel model) {
// 			this.requirementList = requirementList;
// 			this.model = model;
// 		}
		
// 		@Override
// 		    public List<Package> doInBackground() throws java.io.IOException,
// 		    org.xml.sax.SAXException {

// 			String databaseUrl = getConfig().getRepositoryDatabase();

// 			//get download stream
// 			Download d = Download.create(databaseUrl);
// 			int size = d.getSize();
// 			InputStream dl;
// 			if (size > 0) {
// 				ProgressListener progress =
// 				    new SumProgressListener(new PercentageProgressListener(size, this));
// 				dl = d.getStream(progress);
// 			}
// 			else {
// 				dl = d.getStream(null);
// 			}

// 			final PackageDatabaseParser parser = new PackageDatabaseParser();
// 			List<Requirement> all = parser.parse(XmlUtils.getDocument(dl));
			
// 			requirementList.setRequirements(all);
// 			try {
// 				List<PackageFileList> installed = parseInstalled.get();

// 				synchronized (requirementList) {
// 					requirementList.setInstalled(installed);
// 				}
// 			}
// 			catch (java.util.concurrent.ExecutionException e) {}
// 			catch (java.lang.InterruptedException e) {
// 				System.out.println("Couldn't wait for result of installedMaps parse!" + e);
// 			}

// 			List<Package> packages = new ArrayList<Package>(all.size());
// 			for (Requirement r: all) {
// 				if (r instanceof Package) {
// 					packages.add((Package) r);
// 				}
// 			}
// 			return packages;
// 		}

// 		@Override
// 		    public void done() {
// 			try {
// 				maplist.setMapList(get());
// 			}
// 			catch (java.util.concurrent.ExecutionException t) {
// 				Throwable e = t.getCause();
// 				if (e instanceof java.io.IOException) {
// 					String msg = "Couldn't open database file: " + e.getMessage();
// 					Object[] options = {"Try again",
// 					                    "Cancel"};
// 					int tryAgain =
// 					    JOptionPane.showOptionDialog(QuakeInjector.this,
// 					                                 msg,
// 					                                 "Could not access database",
// 					                                 JOptionPane.YES_NO_OPTION,
// 					                                 JOptionPane.WARNING_MESSAGE,
// 					                                 null,
// 					                                 options,
// 					                                 options[1]);
// 					if (tryAgain == 0) {
// 						new DatabaseParser(requirementList, maplist)
// 						    .execute();
// 					}
// 					else {
// 						return;
// 					}
// 				}
// 				else if (e instanceof org.xml.sax.SAXException) {
// 					System.out.println("Couldn't parse xml!" + e);
// 				}
// 			}
// 			catch (java.lang.InterruptedException e) {
// 				throw new RuntimeException("Couldn't get map list!" + e.getMessage());
// 			}
// 		}

// 		public void publish(long progress) {
// 			if (progress <= 100) {
// 				setProgress((int) progress);
// 			}
// 		}
		
// 	}


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
// 		try {
//         // Set System L&F
// 			javax.swing.UIManager.setLookAndFeel(
// 				javax.swing.UIManager.getSystemLookAndFeelClassName());
// 		} 
// 		catch (javax.swing.UnsupportedLookAndFeelException e) {
// 		}
// 		catch (ClassNotFoundException e) {
// 		}
// 		catch (InstantiationException e) {
// 		}
// 		catch (IllegalAccessException e) {
// 		}

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
			config.setMainWindowPositionX((int) bounds.getX());
			config.setMainWindowPositionY((int) bounds.getY());
			config.setMainWindowWidth((int) bounds.getWidth());
			config.setMainWindowHeight((int) bounds.getHeight());
			config.write();
			System.out.println("Closing Window: " + (int) bounds.getWidth()
			    + (int) bounds.getHeight());

			System.exit(0);
		}

	}
	
}