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

	private final Configuration config;
	private final Paths paths;

	private final EngineStarter starter;

	private PackageInteractionPanel interactionPanel;
	private final InstalledPackageList installedMaps;
	private final PackageList maplist;

	public QuakeInjector() {
		super(applicationName);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setLayout(new BoxLayout(getContentPane(),
								BoxLayout.PAGE_AXIS));

		config = new Configuration();

		paths = new Paths(config.get("repositoryBase"));
		
		/** @todo 2009-05-04 14:48 hrehfeld    check if the paths still exist at startup */
		starter = new EngineStarter(new File(config.getEnginePath()),
									new File(config.getEnginePath()
											 + File.separator
											 + config.getEngineExecutable()),
									config.getEngineCommandline());

		maplist = new PackageList();
		installedMaps = new InstalledPackageList();


		createMenu();
		addMainPane(getContentPane());
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
					parse(installedMaps, maplist);
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
					final EngineConfigDialog d
					    = new EngineConfigDialog(QuakeInjector.this,
					                             config.getEnginePath(),
					                             config.getEngineExecutable(),
					                             config.getEngineCommandline(),
					                             config.getRogueInstalled(),
					                             config.getHipnoticInstalled()
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
					
				}});

		setJMenuBar(menuBar);
	}


	private void saveEngineConfig(File enginePath,
								  File engineExecutable,
	                              String commandline,
	                              boolean rogueInstalled,
	                              boolean hipnoticInstalled) {
		setEngineConfig(enginePath, engineExecutable, commandline, rogueInstalled, hipnoticInstalled);

		config.setEnginePath(enginePath.getAbsolutePath());
		config.setEngineExecutable(RelativePath.getRelativePath(enginePath, engineExecutable));
		config.setEngineCommandline(commandline);
		config.setRogueInstalled(rogueInstalled);
		config.setHipnoticInstalled(hipnoticInstalled);
		
		config.write();
	}

	private void setEngineConfig(File enginePath,
								 File engineExecutable,
	                             String commandline,
	                             boolean rogueInstalled,
	                             boolean hipnoticInstalled) {
		starter.setQuakeDirectory(enginePath);
		starter.setQuakeExecutable(engineExecutable);
		starter.setQuakeCommandline(commandline);

		setInstalled("rogue", rogueInstalled);
		setInstalled("hipnotic", hipnoticInstalled);

		interactionPanel.setInstallDirectory(enginePath.getAbsolutePath());
	}

	private void setInstalled(String name, boolean installed) {
		if (installed) {
			installedMaps.put(name, new PackageFileList(name));
			System.out.println("Setting " + name + " to installed.");
		}
		else {
			installedMaps.remove(name);
		}
	}
	

	private void addMainPane(Container panel) {
		panel.setLayout(new GridBagLayout());

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());

		
		//create a table
		final PackageTable table =  new PackageTable(maplist);
		table.setMinimumSize(new Dimension(450, 300));


		{
			JPanel filterPanel = new JPanel(new SpringLayout());
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

		this.interactionPanel = new PackageInteractionPanel(config.getEnginePath(),
												 paths,
												 installedMaps,
												 starter,
												 installQueue);
		maplist.addChangeListener(interactionPanel);

		JPanel infoPanel = new JPanel(new GridBagLayout());

		infoPanel.add(Box.createVerticalGlue(), new GridBagConstraints() {{
			anchor = PAGE_START;
			fill = VERTICAL;
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
			= new PackageListSelectionHandler(interactionPanel,
											  maplist,
											  table);
		table.getSelectionModel().addListSelectionListener(selectionHandler);

		parse(installedMaps, maplist);

	}

	
	private void display() {
		pack();
		setVisible(true);
	}

	private void parse(final InstalledPackageList installedMaps,
					   final PackageList maplist) {
		final PackageDatabaseParser parser = new PackageDatabaseParser();

		SwingWorker<List<Package>,Void> parse = new SwingWorker<List<Package>, Void>() {
			@Override
			public List<Package> doInBackground() throws java.io.IOException,
			org.xml.sax.SAXException {
				try {
					installedMaps.read();
				}
				catch (java.io.IOException e) {
					/** @todo 2009-04-28 19:00 hrehfeld    better error reporting? */
					System.out.println(e.getMessage());
				}
				
				java.util.List<Package> maps = parser.parse(config.getRepositoryDatabase());
				installedMaps.set(maps);
				return maps;
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
							parse(installedMaps, maplist);
						}
						else {
							return;
						}
					}
					else if (e instanceof org.xml.sax.SAXException) {
						System.out.println("Couldn't parse xml!");
					}
				}
				catch (java.lang.InterruptedException e) {
					throw new RuntimeException("Couldn't get map list!" + e.getMessage());
				}
			}
		};
		parse.execute();
		
	}

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					QuakeInjector qs = new QuakeInjector();
					qs.display();
				}
			});

	}
}