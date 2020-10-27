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
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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

public class QuakeInjector extends JFrame {
	/**
	 * Window title
	 */
	private static final String ICON_URL = "/Inject2_SIZE.png";
	private static final String ICON_SIZE_PLACEHOLDER = "SIZE";
	private static final int[] ICON_SIZES = { 16, 32, 48, 256 };
	
	private static final String applicationName = "Quake Injector";
	private static final int minWidth = 300;
	private static final int minHeight = 300;

	private final static String installedMapsFileName = "installedMaps.xml";
	private final static File installedMapsFile = new File(installedMapsFileName);
	private final SaveInstalled saveInstalled = new SaveInstalled(installedMapsFile);
	
	private final static String zipFilesXml = "zipFiles.xml";

	final static File configFile = new File("config.properties");




	private EngineStarter starter;

	/**
	 * @todo 2010-02-09 12:11 hrehfeld    member variable seems unnecessary
	 */
	private PackageInteractionPanel interactionPanel;
	private RequirementList maps;
	/**
	 * @todo 2010-02-09 12:11 hrehfeld    member variable seems unnecessary
	 */
	private PackageList packages;
	/**
	 * @todo 2010-02-09 12:11 hrehfeld    member variable seems unnecessary
	 */
	private final PackageListModel maplist;
	private Installer installer;


	private final InstalledPackages installedMaps = new InstalledPackages();

	/** is offline mode enabled? */
	private Configuration.OfflineMode offline;

	private final Configuration config;

	private final Menu menu;

	public QuakeInjector() {
		super(applicationName);

		//load config
		final Future<Configuration> config = new SwingWorker<Configuration,Void>() {
			@Override public Configuration doInBackground() { return new Configuration(configFile); }
		};
		((SwingWorker<?,?>) config).execute();

		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setLayout(new BoxLayout(getContentPane(),
								BoxLayout.PAGE_AXIS));

		maps = new RequirementList();
		packages = new PackageList(maps);
		maplist = new PackageListModel(packages);

		{
			setIconImages(createIconList(ICON_SIZES, ICON_URL, ICON_SIZE_PLACEHOLDER));
		}

		menu = createMenuBar();
		setJMenuBar(menu);

		setMinimumSize(new Dimension(minWidth, minHeight));
		
		Configuration cfg = null;
		try {
			cfg = config.get();
		}
		catch (ExecutionException e) {
			System.err.println("Couldn't load config: " + e.getCause());
			e.getCause().printStackTrace();
		}
		catch (InterruptedException e) {
			System.err.println("Interrupted: " + e);
		}
		this.config = cfg;

		this.offline = cfg.OfflineMode;

		//config needed here
		addMainPane(getContentPane());

		addWindowListener(new QuakeInjectorWindowListener());
		
		setWindowSize();
	}

	/**
	 * main menu
	 */
	private Menu createMenuBar() {
		ActionListener parseDatabase = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doParseInstalled();
					parseDatabaseAndSetList();
				}
			};

		ActionListener checkInstalled = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					checkForInstalledMaps();
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
		ActionListener offlineModeChanged = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						offline.set(!offline.get());
					}};

		return new Menu(parseDatabase, checkInstalled, quit, showEngineConfig, offlineModeChanged);
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
				// System.out.println("Setting window bounds: "
				//                    + posX + ", "
				//                    + posY + ", "
				//                    + width + ", "
				//                    + height);
			
				setBounds(posX, posY, width, height);
			}
			else {
				// System.out.println("Setting window size: " + width + ", " + height);
				setSize(width, height);
			}
		}
		else {
			pack();
		}
	}
		

	/**
	 * Everything that may be run AFTER the initial window is shown should be run here
	 */
	private void init() {
		doParseInstalled();

		final Future<Void> requirementsListUpdater = parseDatabaseAndSetList();

		Configuration.EnginePath enginePath = getConfig().EnginePath;
		boolean workingDirAtExecutable = false;
		File engineExe = new File("");
		if (getConfig().EngineExecutable.existsOrDefault()) {
			engineExe = new File(enginePath.get()
			                          + File.separator
			                          + getConfig().EngineExecutable);
			workingDirAtExecutable = getConfig().WorkingDirAtExecutable.get();
		}
		File workingDir;
		if (workingDirAtExecutable) {
			workingDir = engineExe.getParentFile();
		}
		else {
			workingDir = enginePath.get();
		}

		starter = new EngineStarter(workingDir,
		                            engineExe,
		                            getConfig().EngineCommandLine);
		installer = new Installer(enginePath,
		                          getConfig().DownloadPath);

		interactionPanel.init(installer,
		                      getConfig().RepositoryBasePath,
		                      maps,
		                      starter,
		                      new SaveInstalled(installedMapsFile)
		    );

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


	private void doParseInstalled() {
		installedMaps.parse(installedMapsFile);
	}

	private InputStream downloadDatabase(String databaseUrl) throws IOException {
		//get download stream
		Download d = Download.create(databaseUrl);
		d.connect();
		InputStream dl;
		//int size = d.getSize();
		// if (size > 0) {
		// 	ProgressListener progress =
		// 	    new SumProgressListener(new PercentageProgressListener(size, this));
		// 	dl = d.getStream(progress);
		// }
		// else {
		dl = d.getStream(null);
		//}
		
		return dl;
	}


	private List<Requirement> parseDatabase(InputStream database)
		throws IOException, org.xml.sax.SAXException {
		final PackageDatabaseParser parser = new PackageDatabaseParser();
		
		List<Requirement> all = parser.parse(XmlUtils.getDocument(database));

		return all;
	}

	/**
	 * Parse the online database
	 */
	private Future<List<Requirement>> doParseDatabase() {
		
		final String databaseUrl = getConfig().RepositoryDatabasePath.get();
		
		final SwingWorker<List<Requirement>, Void> dbParse
		    = new SwingWorker<List<Requirement>,Void>() {
			/** the stream for the database download **/
			private BufferedInputStream downloadStream;
			/** we need to try to download the db to a tmp file first so the old one doesn't get overwritten */
			private File tmpFile;
			/** the stream to the temporary file */
			private BufferedOutputStream tmpWriteStream;
			/** the cached database file **/
			private File cache;
			/** stream from the cached database file, if needed **/
			private BufferedInputStream cacheReadStream;
			/** whether the temporary file was populated with a good DB **/
			private boolean updateCache = false;
			
			private BufferedInputStream cachedDatabaseStream() throws IOException {
				if (cache != null && cache.exists() && cache.canRead()) {
					try {
						return new BufferedInputStream(new FileInputStream(cache));
					}
					catch (IOException e) {}
				}
				throw new IOException("cannot download package database or read local cache");
			}

			@Override
			public List<Requirement> doInBackground() throws IOException, org.xml.sax.SAXException {
				cache = getConfig().LocalDatabaseFile.get();
				cache = cache.getAbsoluteFile();
				InputStream db;
				try {
					//download database and dump to file
					downloadStream = new BufferedInputStream(downloadDatabase(databaseUrl));
					tmpFile = File.createTempFile(cache.getName(), null, cache.getParentFile());
					tmpWriteStream = new BufferedOutputStream(new FileOutputStream(tmpFile));
					db = new DumpInputStream(downloadStream, tmpWriteStream);
					List<Requirement> parseResult = parseDatabase(db);
					updateCache = true;
					return parseResult;
				}
				// if using java 7 we could more nicely do:
				// catch (IOException | org.xml.sax.SAXException e) {
				catch (IOException e) {
					cacheReadStream = cachedDatabaseStream();
					return parseDatabase(cacheReadStream);
				}
				catch (org.xml.sax.SAXException e) {
					cacheReadStream = cachedDatabaseStream();
					return parseDatabase(cacheReadStream);
				}
				catch (HTTPException e) {
					cacheReadStream = cachedDatabaseStream();
					return parseDatabase(cacheReadStream);
				}
			}

			@Override
			public void done() {
				try {
					if (cacheReadStream != null) {
						cacheReadStream.close();
					}
					if (tmpWriteStream != null) {
						tmpWriteStream.close();
					}
					if (downloadStream != null) {
						downloadStream.close();
					}
				}
				catch (IOException e) {}
				if (updateCache == true) {
					if (cache.exists()) {
						if (cache.delete() == false) {
							System.err.println("Couldn't delete the real cache file!");
						}
					}
					if (tmpFile.renameTo(cache) == false) {
						System.err.println("Couldn't move the temporary cache file to the real cache file!");
					}
				}
				else {
					if (tmpFile != null && tmpFile.exists()) {
						tmpFile.delete();
					}
					String msg = "Failed to fetch current database; using previously downloaded info.";
					JOptionPane.showMessageDialog(QuakeInjector.this,
					                              msg,
					                              "Downloading failed!",
					                              JOptionPane.WARNING_MESSAGE);
				}
			}
		};

		final ProgressPopup dbpopup = new ProgressPopup("Downloading package database",
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
	 * See what maps are installed
	 */
	private Future<List<PackageFileList>> checkForInstalledMaps() {
		final File enginePath = getConfig().EnginePath.get();

		final File file = new File(zipFilesXml);

		final CheckInstalled checker
		    = new CheckInstalled(this,
		                         getConfig().ZipContentsDatabaseUrl.get(),
		                         getConfig().EnginePath.get().toString(),
		                         maps,
		        saveInstalled);

		final ProgressPopup dbpopup =
		    new ProgressPopup("Checking for installed maps",
		                      new ActionListener() {

								  public void actionPerformed(ActionEvent e) {
									  checker.cancel(true);
								  }
							  },
		                      QuakeInjector.this);

		checker.addPropertyChangeListener(new PropertyChangeListener() {
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
		checker.execute();
		dbpopup.pack();
		dbpopup.setVisible(true);

		return checker;
	}
	

	/**
	 * Tell maps what maps are already installed
	 */
	void setInstalledStatus(final List<PackageFileList> packages) {
		for (PackageFileList l: packages) {
			maps.setInstalled(l);
		}
		for (Requirement r: maps) {
			//System.out.println(r);
		}
		
		maps.notifyChangeListeners();

		
	}

	private Future<Void> parseDatabaseAndSetList() {
		final Future<List<Requirement>> dbParse = doParseDatabase();

		SwingWorker<Void,Void> waitForInstalledMapsAndDb = new SwingWorker<Void,Void>() {
			@Override public Void doInBackground() throws Exception {
				//just wait
				installedMaps.get();
				dbParse.get();

				return null;
			}

			public void done() {
				List<Requirement> packages = null;
				try {
					packages = dbParse.get();
				}
				catch (InterruptedException e) {
					throw new RuntimeExecutionException("parsing database", e);
				}
				catch (ExecutionException e) {
					String ERROR_MESSAGE = "Database parsing failed!";
					Throwable err = e.getCause();
					String msg = err.getMessage();
					try {
						throw err;
					}
					catch (java.net.UnknownHostException exc) {
						msg = "Couldn't establish connection to the server (" + err.getMessage() + ").";
						offline.set(true);
					}
					catch (Throwable any) { /*do nothing*/; }
					
					JOptionPane.showMessageDialog(QuakeInjector.this,
					                              ERROR_MESSAGE + " " + msg,
					                              ERROR_MESSAGE,
					                              JOptionPane.ERROR_MESSAGE);
					return;
				}

				maps.setRequirements(packages);
				System.out.println("Setting Requirements");

				try {
					setInstalledStatus(installedMaps.get());
				}
				catch (InterruptedException e) {
					System.err.println("Interrupted while getting installed maps" + e);
					e.printStackTrace();
				}
				catch (ExecutionException err) {
					maps.notifyChangeListeners();
					
					try {
						throw err.getCause();
					}
					catch (InstalledPackages.NoInstalledPackagesFileException e) {
						System.err.println(e.getMessage());
					}
					catch (Throwable e) {
						String ERROR_MESSAGE = "Reading installed maps failed!";
						JOptionPane.showMessageDialog(QuakeInjector.this,
						                              ERROR_MESSAGE + " " + e.getMessage(),
						                              ERROR_MESSAGE,
						                              JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};
		waitForInstalledMapsAndDb.execute();
		return waitForInstalledMapsAndDb;
	}

	private void showEngineConfig(boolean rogueInstalled, boolean hipnoticInstalled) {
		final EngineConfigDialog d
		    = new EngineConfigDialog(QuakeInjector.this,
		                             getConfig().EnginePath,
		                             getConfig().EngineExecutable,
		                             getConfig().WorkingDirAtExecutable,
		                             getConfig().DownloadPath,
		                             getConfig().EngineCommandLine,
		                             getConfig().RogueInstalled,
		                             getConfig().HipnoticInstalled
		        );
		d.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					try {
						saveEngineConfig(d.getEnginePath(),
						                 d.getEngineExecutable(),
						                 d.getWorkingDirAtExecutable(),
						                 d.getDownloadPath(),
						                 d.getCommandline(),
						                 d.getRogueInstalled(),
						                 d.getHipnoticInstalled());
					}
					catch (IOException err) {
						savingFailedDialogue(err);
					}
				}
			});

		d.pack();
		d.setLocationRelativeTo(this);
		d.setVisible(true);
		
	}


	private void savingFailedDialogue(IOException e) {
		String msg = "Saving the configuration file failed: " + e.getMessage() + "\n"
		    + "The directory is probably read-only and cannot be set writable automatically (Vista/Win7 bug), try to set write permissions manually." ;
		JOptionPane.showMessageDialog(QuakeInjector.this,
		                              msg,
		                              "Saving configuration failed!",
		                              JOptionPane.ERROR_MESSAGE);
	}

	private void saveEngineConfig(File enginePath,
								  File engineExecutable,
								  boolean workingDirAtExecutable,
	                              File downloadPath,
	                              String commandline,
	                              boolean rogueInstalled,
	                              boolean hipnoticInstalled) throws IOException {
		

		Configuration c = getConfig();
		c.EnginePath.set(enginePath);
		c.EngineExecutable.set(RelativePath.getRelativePath(enginePath, engineExecutable));
		c.WorkingDirAtExecutable.set(workingDirAtExecutable);
		c.EngineCommandLine.set(commandline);
		c.RogueInstalled.set(rogueInstalled);
		c.HipnoticInstalled.set(hipnoticInstalled);

		c.DownloadPath.set(downloadPath);

		File workingDir;
		if (workingDirAtExecutable) {
			workingDir = engineExecutable.getParentFile();
		}
		else {
			workingDir = enginePath;
		}

		setEngineConfig(workingDir, engineExecutable, getConfig().EngineCommandLine, rogueInstalled, hipnoticInstalled);


		try {
			c.write();
		}
		catch (IOException e) {
			File dir = configFile.getAbsoluteFile().getParentFile();
			System.out.println("Trying to set directory (" + dir + ") writable..");
			try {
				dir.setWritable(true);
			}
			catch (SecurityException securityError) {
				System.out.println("Couldn't set writable: " + securityError);
			}

			c.write();
		}
	}

	/**
	 * @todo 2010-02-09 12:19 hrehfeld    Let this use configuration values to their full extent
	 */
	private void setEngineConfig(File workingDir,
								 File engineExecutable,
	                             Configuration.EngineCommandLine commandline,
	                             boolean rogueInstalled,
	                             boolean hipnoticInstalled) {
		starter.setWorkingDirectory(workingDir);
		starter.setQuakeApplication(engineExecutable);
		starter.setQuakeCommandline(commandline);

		maps.get("rogue").setInstalled(rogueInstalled);
		maps.get("hipnotic").setInstalled(hipnoticInstalled);
		try {
			synchronized (maps) {
				saveInstalled.write(maps);
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
			filterPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

			final JButton clearFilter = new JButton("Clear");
			clearFilter.setEnabled(false);  // disabled until there's text in filter textfield

			final JTextField filter = new JTextField();
			filter.getDocument().addDocumentListener(
                new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) { filter(); }
                    public void insertUpdate(DocumentEvent e) { filter(); }
                    public void removeUpdate(DocumentEvent e) { filter(); }

					private void filter() {
						table.getRowSorter().setRowFilter(maplist.filter(filter.getText()));

						// https://stackoverflow.com/questions/21522902/how-disable-button-when-nothing-in-textfield
						if (filter.getText().equals("")) {
							clearFilter.setEnabled(false);
						} else {
							clearFilter.setEnabled(true);
						}
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

			// https://stackoverflow.com/questions/5328945/how-to-clear-the-jtextfield-by-clicking-jbutton
			clearFilter.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					filter.setText("");
				}
			});

			filterPanel.add(clearFilter, new GridBagConstraints() {{
				anchor = LINE_END;
			}});

			final JButton randomMapButton = new JButton("Install Random Map");
			randomMapButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					// Package list index != current table index, which can change based on the column used to sort the
					// table. Therefore, it has to be converted.
					int mapTableRowIdx = new Random().nextInt(maplist.getRowCount());
					int mapListIdx = table.getRowSorter().convertRowIndexToModel(mapTableRowIdx);
					interactionPanel.install(maplist.getPackage(mapListIdx), false);
					table.setRowSelectionInterval(mapTableRowIdx, mapTableRowIdx);
					table.scrollRectToVisible(new Rectangle(table.getCellRect(mapTableRowIdx, 0, true)));
				}
			});

			filterPanel.add(randomMapButton, new GridBagConstraints() {{
				anchor = LINE_END;
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
		
		Configuration config = getConfig();
		PackageDetailPanel details = new PackageDetailPanel(config.ScreenshotRepositoryPath.get());
		
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

	private Configuration getConfig() {
		if (config == null) {
			throw new RuntimeException("Config not initialised!");
		}
		return config;
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
			CABundleLoader.loadCertificateAuthorities();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
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

		// borrowed from jmtd's wadc:
		// The default setting for useSystemAAFontSettings is off; and the result
		// looks awful on (at least my) Linux systems. We want to switch the default
		// to on, but leave it possible for the user to override our choice.
		if(null == System.getenv("_JAVA_OPTIONS") ||
				!System.getenv("_JAVA_OPTIONS").contains("useSystemAAFontSettings"))
		{
				System.setProperty("awt.useSystemAAFontSettings", "on");
		}

		// override the HTTP user-agent for any connections this program does
		// re https://stackoverflow.com/questions/2529682/setting-user-agent-of-a-java-urlconnection
		System.setProperty("http.agent", "Quakeinjector-" + BuildCommit.buildCommit);

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
			if (installer.working()) {
				String msg = "There are maps left in the install queue. Wait until they are finished installing?";

				Object[] options = {"Wait",
				                    "Close immediately"};
				int optionDialog =
				    JOptionPane.showOptionDialog(QuakeInjector.this,
				                                 msg,
				                                 "Maps still installing",
				                                 JOptionPane.YES_NO_OPTION,
				                                 JOptionPane.WARNING_MESSAGE,
				                                 null,
				                                 options,
				                                 options[0]);
				if (optionDialog == 0) {
					setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					return;
				}
				else {
					installer.cancelAll();
					setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				}
			}
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

			try {
				config.write();
			}
			catch (IOException err) {
				savingFailedDialogue(err);
			}
			//System.out.println("Closing Window: " + (int) bounds.getWidth() + (int) bounds.getHeight());


			System.exit(0);
		}

	}

	private static List<Image> createIconList(int[] iconSizes, String iconUrl, String sizeToken) {
			List<Image> icons = new ArrayList<Image>(iconSizes.length);
			for (int size: iconSizes) {
				String path = iconUrl.replace(sizeToken, Integer.toString(size));
				try {
					javax.swing.ImageIcon icon = Utils.createImageIcon(path, "Icon" + size);
					icons.add(icon.getImage());
				}
				catch (IOException e) {
					System.err.println("WARNING: Couldn't load icon file " + path);
				}
			}
			return icons;
	}
	
}
