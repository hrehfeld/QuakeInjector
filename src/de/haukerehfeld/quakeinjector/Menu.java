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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;

public class Menu extends JMenuBar {
		private JCheckBoxMenuItem enableOfflineMode;
		
		public Menu(ActionListener databaseParser,
		            ActionListener checkForInstalledMaps,
		            ActionListener quitter,
		            ActionListener showEngineConfig,
		            ActionListener offlineAction) {
				setOpaque(true);
				// 		setPreferredSize(new Dimension(200, 20));

				JMenu fileMenu = new JMenu("File");
				add(fileMenu);

				JMenuItem reparseDatabase = new JMenuItem("Reload database", KeyEvent.VK_R);
				reparseDatabase.addActionListener(databaseParser);
				fileMenu.add(reparseDatabase);

				JMenuItem checkInstalled = new JMenuItem("Check for installed maps (experimental!)", KeyEvent.VK_C);
				checkInstalled.addActionListener(checkForInstalledMaps);
				fileMenu.add(checkInstalled);

				enableOfflineMode = new JCheckBoxMenuItem("Offline Mode");
				checkInstalled.addActionListener(offlineAction);
				fileMenu.add(enableOfflineMode);
				

				JMenuItem quit = new JMenuItem("Quit", KeyEvent.VK_T);
				quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
				quit.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
				quit.addActionListener(quitter);
				fileMenu.add(quit);

				JMenu configM = new JMenu("Configuration");
				add(configM);

				JMenuItem engine = new JMenuItem("Engine Configuration");
				configM.add(engine);
				engine.addActionListener(showEngineConfig);
		}

		public void setOfflineMode(boolean offline) {
				enableOfflineMode.setSelected(offline);
		}
}