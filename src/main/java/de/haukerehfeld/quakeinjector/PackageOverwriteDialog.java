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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import de.haukerehfeld.quakeinjector.gui.OkayCancelApplyPanel;
import de.haukerehfeld.quakeinjector.gui.ScrollablePanel;

public class PackageOverwriteDialog extends JDialog {
	private final static String windowTitle = "Overwrite Package Files?";

	private final static int BUTTONMARGIN = 6;

	private final static int MARGINWIDTH = 5;
	
	private final static int DESCRIPTIONMARGINWIDTH = 5 + MARGINWIDTH;
	private final static int DESCRIPTIONMARGINHEIGHT = 10;
	private final static Border DESCRIPTIONMARGIN
	= BorderFactory.createEmptyBorder(DESCRIPTIONMARGINHEIGHT,
	                                  DESCRIPTIONMARGINWIDTH,
	                                  DESCRIPTIONMARGINHEIGHT,
	                                  DESCRIPTIONMARGINWIDTH);	

	private final static int LINEENTRYMARGINWIDTH = 4 + MARGINWIDTH;
	private final static int LINEENTRYMARGINHEIGHT = 2;
	private final static Border LINEENTRYMARGIN
	= BorderFactory.createEmptyBorder(LINEENTRYMARGINHEIGHT,
	                                  LINEENTRYMARGINWIDTH,
	                                  LINEENTRYMARGINHEIGHT,
	                                  LINEENTRYMARGINWIDTH);	

	private final static int CHECKBOXINDENT = 17;
	private final static int CHECKBOXEXTRAMARGIN = 0;
	private final static Border NOCHECKBOXLINEENTRYMARGIN
	= BorderFactory.createEmptyBorder(LINEENTRYMARGINHEIGHT + CHECKBOXEXTRAMARGIN,
	                                  LINEENTRYMARGINWIDTH + CHECKBOXINDENT,
	                                  LINEENTRYMARGINHEIGHT + CHECKBOXEXTRAMARGIN,
	                                  LINEENTRYMARGINWIDTH);	
	
	private final static int SECTIONMARGIN = 15;

	private final JFrame parent;

	private final JPanel panel;

	private boolean canceled = true;

	private ArrayList<String> overwriteList = new ArrayList<String>();
	private ArrayList<String> alwaysWriteList = new ArrayList<String>();

	private final Map<String,JCheckBox> overwriteBoxes = new HashMap<String,JCheckBox>();
	
	public PackageOverwriteDialog(final JFrame frame) {
		super(frame, windowTitle, true);
		this.parent = frame;

		panel = new ScrollablePanel(50, 50);
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(false);

		JScrollPane panelScroll = new JScrollPane(panel);
		panelScroll.getViewport().setBackground(javax.swing.UIManager.getColor("TextPane.background"));
		add(panelScroll, BorderLayout.CENTER);

		final JButton okay = new JButton("Write files");
		final JButton cancel = new JButton("Cancel");

		okay.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					canceled = false;
					
					setVisible(false);
					dispose();
				}
			});
		cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					dispose();
				}
			});

		{
			
			
			add(new OkayCancelApplyPanel(okay, cancel, null, false), BorderLayout.PAGE_END);
		}
		
	}

	private void addDescriptionLabel(final String text, final int line) {
		JLabel description = new JLabel(text);
		description.setLabelFor(this);
		description.setOpaque(true);
		description.setBorder(DESCRIPTIONMARGIN);
		panel.add(description, new GridBagConstraints() {{
			gridy = line;

			gridwidth = 2;
			fill = BOTH;
			anchor = LINE_START;
		}});
	}

	private void addDescription() {
		addDescriptionLabel(windowTitle, 0);
	}

	private void addAlwaysWriteDescription(final int line) {
		addDescriptionLabel("Files that need to be installed:", line);
	}
	

	public void addFile(String name, boolean overwrite) {
		List<String> list = (overwrite ? overwriteList : alwaysWriteList);
		list.add(name);
	}

	public List<String> getOverwritten() {
		List<String> overwritten = new ArrayList<String>(alwaysWriteList);
		for (Map.Entry<String,JCheckBox> e: overwriteBoxes.entrySet()) {
			String name = e.getKey();
			JCheckBox box = e.getValue();
			if (box.isSelected()) {
				overwritten.add(name);
			}
		}

		return overwritten;
	}

	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * Only call once
	 */
	public void packAndShow() {
		addDescription();
		int lines = 1;

		java.util.Collections.sort(overwriteList);
		for (String name: overwriteList) {
			JCheckBox overwrite = new JCheckBox(name);
			overwrite.setSelected(true);
			overwrite.setOpaque(false);
			overwrite.setBorder(LINEENTRYMARGIN);
			final int finalLines = lines;
			panel.add(overwrite, new OverwriteBoxConstraints() {{
				gridy = finalLines;
			}});
			overwriteBoxes.put(name, overwrite);
			++lines;
		}

		if (!alwaysWriteList.isEmpty()) {
			{
				final int finalLines = lines++;
				    panel.add(Box.createRigidArea(new Dimension(0,SECTIONMARGIN)),
				              new GridBagConstraints() {{
								  gridy = finalLines;
							  }});
			}
			java.util.Collections.sort(alwaysWriteList);
			addAlwaysWriteDescription(lines++);

			for (String name: alwaysWriteList) {
				JLabel listEntry = new JLabel(name);
				listEntry.setOpaque(false);
				listEntry.setBorder(NOCHECKBOXLINEENTRYMARGIN);
				final int finalLines = lines;
				panel.add(listEntry, new OverwriteBoxConstraints() {{
					gridy = finalLines;
				}});
				++lines;
			}
		}
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	class OverwriteBoxConstraints extends GridBagConstraints {{
		anchor = LINE_START;
		fill = HORIZONTAL;
		weightx = 1;
	}};

}