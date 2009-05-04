package de.haukerehfeld.quakeinjector;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.ArrayList;

import java.io.File;

public class QuakeInjector {
	/**
	 * Window title
	 */
	private static final String applicationName = "Quake Injector";

	private final Configuration config;
	private final Paths paths;

	private final EngineStarter starter;

	public QuakeInjector() {
		config = new Configuration();

		paths = new Paths(config.get("repositoryBase"),
						  config.get("enginePath"));
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
					EngineConfigDialog d = new EngineConfigDialog(frame,
																  config.getEnginePath(),
																  config.getEngineExecutable(),
																  config.getEngineCommandline());
					saveEngineConfig(d.getEnginePath(),
									 d.getEngineExecutable(),
									 d.getCommandline());
					
				}});
	}


	private void saveEngineConfig(String enginePath,
								  String engineExecutable,
								  String commandline) {
		setEngineConfig(enginePath, engineExecutable, commandline);

		config.setEnginePath(enginePath);
		config.setEngineExecutable(engineExecutable);
		config.setEngineCommandline(commandline);
		
		config.write();
	}

	private void setEngineConfig(String enginePath,
								 String engineExecutable,
								 String commandline) {
		starter.setQuakeDirectory(new File(enginePath));
		starter.setQuakeExecutable(new File(enginePath + File.separator + engineExecutable));
		starter.setQuakeCommandline(commandline);
	}
	

	private void addMainPane(Container panel) {

		final MapList maplist = new MapList();
		
		//create a table
		final JTable table = new JTable(maplist);
		table.setPreferredScrollableViewportSize(new Dimension(500, 600));
		table.setFillsViewportHeight(true);
		table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);


		//Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);

		panel.add(scrollPane);

		final InstalledMaps installedMaps = new InstalledMaps();

		MapInfoPanel interactionPanel = new MapInfoPanel(paths, installedMaps, starter);
		maplist.addChangeListener(interactionPanel);
		panel.add(interactionPanel);
		ShowMapInfoSelectionHandler selectionHandler = new ShowMapInfoSelectionHandler(interactionPanel,
																					   maplist);
		table.getSelectionModel().addListSelectionListener(selectionHandler);

		final MapInfoParser parser = new MapInfoParser();
		SwingWorker<ArrayList<MapInfo>,Void> parse = new SwingWorker<ArrayList<MapInfo>, Void>() {
			@Override
			public ArrayList<MapInfo> doInBackground() {
				try {
					installedMaps.read();
				}
				catch (java.io.IOException e) {
					/** @todo 2009-04-28 19:00 hrehfeld    better error reporting? */
					System.out.println(e.getMessage());
				}
				ArrayList<MapInfo> maps = parser.parse();
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