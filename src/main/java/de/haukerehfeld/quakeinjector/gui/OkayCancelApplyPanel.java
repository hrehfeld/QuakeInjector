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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class OkayCancelApplyPanel extends JPanel {
	private static final Insets LEFTBORDER = new Insets(LookAndFeelDefaults.FRAMEPADDING,
	                                                    LookAndFeelDefaults.FRAMEPADDING,
	                                                    LookAndFeelDefaults.FRAMEPADDING,
	                                                    LookAndFeelDefaults.FRAMEPADDING);

	private static final Insets OTHERBORDER = new Insets(LookAndFeelDefaults.FRAMEPADDING,
	                                                     0,
	                                                     LookAndFeelDefaults.FRAMEPADDING,
	                                                     LookAndFeelDefaults.FRAMEPADDING);

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