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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import java.util.Map;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.haukerehfeld.quakeinjector.gui.ErrorEvent;
import de.haukerehfeld.quakeinjector.gui.ErrorListener;
import de.haukerehfeld.quakeinjector.gui.JPathPanel;

public class PackageOverwriteDialog extends JDialog {
	private final static String windowTitle = "Overwrite Package Files?";

	private final JPanel fileListPanel;
	private int fileListRows = 0;

	private boolean canceled = false;

	private final Map<String,JCheckBox> overwriteBoxes = new HashMap<String,JCheckBox>();
	
	public PackageOverwriteDialog(final JFrame frame) {
		super(frame, windowTitle, true);

		JLabel description = new JLabel("The following files already exist:", SwingConstants.CENTER);
		description.setLabelFor(this);
		description.setPreferredSize(new Dimension(100, 50));
		add(description, BorderLayout.PAGE_START);

		fileListPanel = new JPanel();
		fileListPanel.setLayout(new GridBagLayout());
		add(new JScrollPane(fileListPanel), BorderLayout.CENTER);

		final JButton okay = new JButton("Overwrite files");
		final JButton cancel = new JButton("Cancel");

		okay.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					dispose();
				}
			});
		cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					canceled = true;
					
					setVisible(false);
					dispose();
				}
			});

		{
			JPanel okayCancelPanel = new JPanel();
			okayCancelPanel.setLayout(new BoxLayout(okayCancelPanel, BoxLayout.LINE_AXIS));
			okayCancelPanel.add(Box.createHorizontalGlue());
			okayCancelPanel.add(cancel);
			okayCancelPanel.add(Box.createRigidArea(new Dimension(10,0)));
			okayCancelPanel.add(okay);
			okayCancelPanel.add(Box.createHorizontalGlue());
			
			add(okayCancelPanel, BorderLayout.PAGE_END);
		}
		
	}

	public void addFile(String name, boolean enabled) {
		JCheckBox overwrite = new JCheckBox(name);
		overwrite.setSelected(true);
		overwrite.setEnabled(enabled);
		fileListPanel.add(overwrite, new OverwriteBoxConstraints() {{
			gridy = fileListRows;
		}});
		overwriteBoxes.put(name, overwrite);
		++fileListRows;
	}

	public boolean overwrite(String name) {
		JCheckBox box = overwriteBoxes.get(name);
		return box != null && box.isSelected();
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void packAndShow() {
		pack();
		setVisible(true);
	}

	class FileNameConstraints extends GridBagConstraints {{
		anchor = LINE_START;
		fill = NONE;
		gridx = 0;
		gridwidth = 1;
		gridheight = 1;
		weightx = 0;
		weighty = 0;
		
	}};
	class OverwriteBoxConstraints extends GridBagConstraints {{
		anchor = LINE_END;
		fill = HORIZONTAL;
		gridx = 1;
		gridwidth = 2;
		gridheight = 1;
		weightx = 1;
		weighty = 0;
	}};

}