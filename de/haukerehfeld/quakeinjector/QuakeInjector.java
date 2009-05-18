package de.haukerehfeld.quakeinjector;

//import java.awt.*;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Container;
import java.awt.Dimension;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableRowSorter;

import java.util.ArrayList;
import java.util.List;

import java.io.File;

public class QuakeInjector {
	/**
	 * Window title
	 */
	private static final String applicationName = "Quake Injector";

	private final Configuration config;
	private final Paths paths;

	private final EngineStarter starter;

	private PackageInteractionPanel interactionPanel;

	public QuakeInjector() {
		config = new Configuration();

		paths = new Paths(config.get("repositoryBase"));
		
		/** @todo 2009-05-04 14:48 hrehfeld    check if the paths still exist at startup */
		starter = new EngineStarter(new File(config.getEnginePath()),
									new File(config.getEnginePath()
											 + File.separator
											 + config.getEngineExecutable()),
									config.getEngineCommandline());
	}

	private void createMenu(final JFrame frame) {
		JMenuBar menuBar = new JMenuBar();
		menuBar.setOpaque(true);
		menuBar.setPreferredSize(new Dimension(200, 20));
		frame.setJMenuBar(menuBar);

		JMenu menu = new JMenu("File");
		menuBar.add(menu);

		JMenuItem menuItem = new JMenuItem("Quit",
										   KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,
													   ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
			"This doesn't really do anything");
		menu.add(menuItem);


		JMenu configM = new JMenu("Configuration");
		menuBar.add(configM);

		JMenuItem engine = new JMenuItem("Engine Configuration");
		configM.add(engine);
		engine.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final EngineConfigDialog d = new EngineConfigDialog(frame,
																  config.getEnginePath(),
																  config.getEngineExecutable(),
																  config.getEngineCommandline());
					d.addChangeListener(new ChangeListener() {
							public void stateChanged(ChangeEvent e) {
								saveEngineConfig(d.getEnginePath(),
												 d.getEngineExecutable(),
												 d.getCommandline());
							}
						});

					d.packAndShow();
					
				}});
	}


	private void saveEngineConfig(File enginePath,
								  File engineExecutable,
								  String commandline) {
		setEngineConfig(enginePath, engineExecutable, commandline);

		config.setEnginePath(enginePath.getAbsolutePath());
		config.setEngineExecutable(RelativePath.getRelativePath(enginePath, engineExecutable));
		config.setEngineCommandline(commandline);
		
		config.write();
	}

	private void setEngineConfig(File enginePath,
								 File engineExecutable,
								 String commandline) {
		starter.setQuakeDirectory(enginePath);
		starter.setQuakeExecutable(engineExecutable);
		starter.setQuakeCommandline(commandline);

		interactionPanel.setInstallDirectory(enginePath.getAbsolutePath());
	}
	

	private void addMainPane(Container panel) {
		panel.setLayout(new GridBagLayout());

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());

		final PackageList maplist = new PackageList();
		
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

		final InstalledPackageList installedMaps = new InstalledPackageList();

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
		
		ShowMapInfoSelectionHandler selectionHandler
			= new ShowMapInfoSelectionHandler(interactionPanel,
											  maplist,
											  table);
		table.getSelectionModel().addListSelectionListener(selectionHandler);

		final PackageDatabaseParser parser = new PackageDatabaseParser();
		SwingWorker<List<Package>,Void> parse = new SwingWorker<List<Package>, Void>() {
			@Override
			public List<Package> doInBackground() {
				try {
					installedMaps.read();
				}
				catch (java.io.IOException e) {
					/** @todo 2009-04-28 19:00 hrehfeld    better error reporting? */
					System.out.println(e.getMessage());
				}
				java.util.List<Package> maps = parser.parse();
				installedMaps.set(maps);
				return maps;
			}

			@Override
			public void done() {
				try {
					maplist.setMapList(get());
				}
				catch (java.lang.InterruptedException e) {
					throw new RuntimeException("Couldn't get map list!" + e.getMessage());
				}
				catch (java.util.concurrent.ExecutionException e) {
					throw new RuntimeException("Couldn't get map list!" + e.getMessage());
				}
			}
		};
		parse.execute();

	}

	
	private void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame(applicationName);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane()
			.setLayout(new BoxLayout(frame.getContentPane(),
									 BoxLayout.PAGE_AXIS));

		createMenu(frame);


		//Add the scroll pane to this panel.
		addMainPane(frame.getContentPane());

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					QuakeInjector qs = new QuakeInjector();
					qs.createAndShowGUI();
				}
			});

	}
}