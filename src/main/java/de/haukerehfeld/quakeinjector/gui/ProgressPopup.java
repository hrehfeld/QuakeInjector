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

//import java.awt.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class ProgressPopup extends JDialog {
	private JProgressBar progress;
	private JButton cancelButton;
	private String description;

	public ProgressPopup(String description, ActionListener cancel, JFrame frame) {
		super(frame, description, true);
		setLocationRelativeTo(frame);
		
		this.description = description;
		
		setLayout(new GridBagLayout());

		progress = new JProgressBar();
		progress.setString(description);
		progress.setValue(0);
		progress.setIndeterminate(true);
		progress.setStringPainted(true);

		add(progress, new GridBagConstraints() {{
			anchor = CENTER;
			gridx = 0;
			fill = BOTH;
			weightx = 1;
		}});

		JButton cancelButton;
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(cancel);
		add(cancelButton, new GridBagConstraints() {{
			anchor = CENTER;
			fill = BOTH;
			gridy = 1;
		}});
	}

	/**
	 * Change progress
	 *
	 * @param percent percent [0, 100]
	 */
	public void setProgress(int percent) {
		progress.setIndeterminate(false);
		progress.setValue(percent);
		progress.setString(progressString(description, percent));

	}

	public void close() {
		setVisible(false);
		dispose();
	}

	public static String progressString(String description, String status) {
		return description + ": " + status;
	}

	public static String progressString(String description, int progress) {
		return progressString(description, progress + "%");
	}
	
}

