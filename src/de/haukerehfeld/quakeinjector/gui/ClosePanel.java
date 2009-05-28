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
package de.haukerehfeld.quakeinjector.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * The typical okay/cancel area
 */
public class ClosePanel extends JPanel {
	public ClosePanel(final JDialog dialog, final ActionListener action) {
		super();
		setLayout(new GridLayout(0,2));
	
		add(makeCloseButton(dialog, "Okay", action));
		add(makeCloseButton(dialog, "Cancel"));
	}
		
	public static JButton makeCloseButton(final JDialog dialog,
									   final String text) {
		return makeCloseButton(dialog, text, null);
	}

	/**
	 * Make a button that closes the dialog and executes another action.
	 * 
	 * You still have to add it to the frame.
	 *
	 * Action may be null.
	 */
	public static JButton makeCloseButton(final JDialog dialog,
									   final String text,
									   final ActionListener action) {
		JButton okay = new JButton(text);
		okay.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
					if (action != null) {
						action.actionPerformed(e);
					}
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            });
		return okay;
	}

	
}