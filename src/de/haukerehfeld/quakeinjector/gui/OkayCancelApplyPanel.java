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

		class C extends GridBagConstraints {{
			insets = OTHERBORDER;
			anchor = LINE_END;
			fill = NONE;
		}};

		//right aligned
		add(Box.createHorizontalGlue(),
		    new GridBagConstraints() {{
				insets = LEFTBORDER;
				gridx = 0;
				weightx = 1;
			}});
		add(okay, new C() {{ gridx = 1; }});
		if (cancel != null) {
			add(cancel, new C() {{ gridx = 2; }});
		}
		if (apply != null && useApply) {
			add(apply, new C() {{ gridx = 3; }});
			apply.setEnabled(false);
		}
	}

	public void setApplyEnabled(boolean enabled) {
		this.apply.setEnabled(enabled);
	}
}