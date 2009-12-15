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

import java.util.List;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Box;

import java.awt.Insets;

public class OkayCancelApplyPanel extends JPanel {
	private static final int MARGIN = 7;
	private static final Insets LEFTBORDER = new Insets(MARGIN,
	                                                    MARGIN,
	                                                    MARGIN,
	                                                    MARGIN);

	private static final Insets OTHERBORDER = new Insets(MARGIN,
	                                                     0,
	                                                     MARGIN,
	                                                     MARGIN);

	private final JButton okay;
	private final JButton cancel;
	private final JButton apply;

	public OkayCancelApplyPanel(JButton okay, JButton cancel, JButton apply, boolean useApply) {
		setLayout(new GridBagLayout());

		this.okay = okay;
		this.cancel = cancel;
		this.apply = apply;


		List<JComponent> components = new ArrayList<JComponent>(3);

		components.add(okay);

		if (cancel != null) {
			components.add(cancel);
		}

		if (apply != null) {
			apply.setEnabled(false);
			components.add(apply);
		}

		OkayCancelApplyPanel.addRow(this, 0, components);
	}

	public void setApplyEnabled(boolean enabled) {
		this.apply.setEnabled(enabled);
	}

	/**
	 * Add some components right aligned to a gridbaglayout container
	 * at a specific row, with proper borders.
	 */
	public static void addRow(final java.awt.Container container,
	                          final int row,
	                          final List<JComponent> components) {
		//right aligned
		container.add(Box.createHorizontalGlue(), new LeftConstraints());

		int col = 1;
		for (JComponent c: components) {
			final int col_ = col;
			container.add(c, new RightConstraints() {{ gridx = col_; gridy = row; }});
			col++;
		}
	}

	static class LeftConstraints extends GridBagConstraints {{
		insets = LEFTBORDER;
		gridx = 0;
		weightx = 1;
	}}

	static class RightConstraints extends GridBagConstraints {{
		insets = OTHERBORDER;
		anchor = LINE_END;
		fill = NONE;
	}}
	
}