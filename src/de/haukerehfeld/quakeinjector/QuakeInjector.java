package de.haukerehfeld.quakeinjector;

//import java.awt.*;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
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
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class QuakeInjector extends JFrame {
	/**
	 * Window title
	 */
	private static final String applicationName = "Quake Injector";

	private Paths paths;

	private EngineStarter starter;

	private PackageInteractionPanel interactionPanel;
	private PackageList maps;
	private final PackageListModel maplist;
	private Installer installer;

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
				System.out.println("Error: InstalledMaps xml couldn't be loaded: " + e.getMessage());
				
				return new ArrayList<PackageFileList>();
			}
				
			return files;
		}

			
	};
	
	private final SwingWorker<Configuration,Void> loadConfig
	    = new SwingWorker<Configuration,Void>() {
		@Override
		public Configuration doInBackground() {
			return new Configuration();
		}
	};

	public QuakeInjector() {
		super(applicationName);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setLayout(new BoxLayout(getContentPane(),
								BoxLayout.PAGE_AXIS));

		maplist = new PackageListModel();
		maps = new PackageList();


		createMenu();
		addMainPane(getContentPane());
	}

	private void init() {
		loadConfig.execute();
		parseInstalled.execute();
		
		final DatabaseParser dbParse = new DatabaseParser(maps, maplist);
		dbParse.execute();

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
		                      maps,
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

	private synchronized Configuration getConfig() {
		try {
			return loadConfig.get();
		}
		catch (InterruptedException e) { }
		catch (java.util.concurrent.ExecutionException e) {}
		return null;
	}

	private synchronized List<PackageFileList> getInstalledFileLists() {
		try {
			return parseInstalled.get();
		}
		catch (InterruptedException e) { }
		catch (java.util.concurrent.ExecutionException e) {}
		return new ArrayList<PackageFileList>();
	}
	
	private void createMenu() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.setOpaque(true);
		menuBar.setPreferredSize(new Dimension(200, 20));

		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		JMenuItem reparseDatabase = new JMenuItem("Reload database", KeyEvent.VK_R);
		reparseDatabase.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new DatabaseParser(maps, maplist)
					    .execute();
				}
			});
		fileMenu.add(reparseDatabase);
		

		JMenuItem quit = new JMenuItem("Quit", KeyEvent.VK_T);
		quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
		quit.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
		fileMenu.add(quit);

		JMenu configM = new JMenu("Configuration");
		menuBar.add(configM);

		JMenuItem engine = new JMenuItem("Engine Configuration");
		configM.add(engine);
		engine.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showEngineConfig(maps.getRequirement("rogue").isInstalled(),
		                             maps.getRequirement("hipnotic").isInstalled());
				}});

		setJMenuBar(menuBar);
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
		panel.setLayout(new GridBagLayout());

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());

		
		//create a table
		final PackageTable table =  new PackageTable(maplist);
		table.setMinimumSize(new Dimension(450, 300));


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
				gridx = 0;
				gridy = 0;
				gridwidth = 1;
				gridheight = 1;
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
				weightx = weighty = 1;
			}});

		panel.add(mainPanel, new GridBagConstraints() {{
			gridx = 0;
			gridy = 0;
			weightx = weighty = 1;
			fill = BOTH;
		}});

		final InstallQueuePanel installQueue = new InstallQueuePanel();

		this.interactionPanel = new PackageInteractionPanel(this, installQueue);
		maplist.addChangeListener(interactionPanel);

		JPanel infoPanel = new JPanel(new GridBagLayout());

		PackageDetailPanel details = new PackageDetailPanel();
		infoPanel.add(details, new GridBagConstraints() {{
			anchor = PAGE_START;
			fill = BOTH;
			weightx = 0;
			weighty = 1;
		}});

		infoPanel.add(interactionPanel, new GridBagConstraints() {{
			gridy = 1;
			fill = BOTH;
		}});

		JScrollPane queueScroll = new JScrollPane(installQueue);
		infoPanel.add(queueScroll, new GridBagConstraints() {{
			anchor = PAGE_END;
			fill = BOTH;
			gridy = 2;
		}});

		panel.add(infoPanel, new GridBagConstraints() {{
			gridx = 1;
			gridy = 0;
			weightx = 0;
			weighty = 1;
			fill = BOTH;
		}});
		
		PackageListSelectionHandler selectionHandler
			= new PackageListSelectionHandler(maplist,
											  table);
		table.getSelectionModel().addListSelectionListener(selectionHandler);
		selectionHandler.addSelectionListener(interactionPanel);
		selectionHandler.addSelectionListener(details);

	}

	
	private void display() {
		pack();
		setVisible(true);
	}

	public class DatabaseParser extends SwingWorker<List<Package>, Void> {
		private final PackageList requirementList;
		private final PackageListModel model;
		
		public DatabaseParser(final PackageList requirementList,
		                      final PackageListModel model) {
			this.requirementList = requirementList;
			this.model = model;
		}
		
		@Override
		    public List<Package> doInBackground() throws java.io.IOException,
		    org.xml.sax.SAXException {
			final PackageDatabaseParser parser = new PackageDatabaseParser();
			List<Requirement> all = parser.parse(getConfig().getRepositoryDatabase());

			requirementList.setRequirements(all);
			try {
				requirementList.setInstalled(parseInstalled.get());
			}
			catch (java.util.concurrent.ExecutionException e) {}
			catch (java.lang.InterruptedException e) {
				System.out.println("Couldn't wait for result of installedMaps parse!" + e);
			}

			List<Package> packages = new ArrayList<Package>(all.size());
			for (Requirement r: all) {
				if (r instanceof Package) {
					packages.add((Package) r);
				}
			}
			return packages;
		}

		@Override
		    public void done() {
			try {
				maplist.setMapList(get());
			}
			catch (java.util.concurrent.ExecutionException t) {
				Throwable e = t.getCause();
				if (e instanceof java.io.IOException) {
					String msg = "Couldn't open database file: " + e.getMessage();
					Object[] options = {"Try again",
					                    "Cancel"};
					int tryAgain =
					    JOptionPane.showOptionDialog(QuakeInjector.this,
					                                 msg,
					                                 "Could not access database",
					                                 JOptionPane.YES_NO_OPTION,
					                                 JOptionPane.WARNING_MESSAGE,
					                                 null,
					                                 options,
					                                 options[1]);
					if (tryAgain == 0) {
						new DatabaseParser(requirementList, maplist)
						    .execute();
					}
					else {
						return;
					}
				}
				else if (e instanceof org.xml.sax.SAXException) {
					System.out.println("Couldn't parse xml!" + e);
				}
			}
			catch (java.lang.InterruptedException e) {
				throw new RuntimeException("Couldn't get map list!" + e.getMessage());
			}
		}
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
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					QuakeInjector qs = new QuakeInjector();
					qs.display();
					qs.init();
				}
			});

	}
}