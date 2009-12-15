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
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Box;

import java.awt.Insets;

public class LookAndFeelDefaults extends JPanel {
	public static final int FRAMEPADDING = 7;

	public static final Border PADDINGBORDER = BorderFactory.createEmptyBorder(FRAMEPADDING,
	                                                                           FRAMEPADDING,
	                                                                           0,
	                                                                           FRAMEPADDING);

	public static final int DIALOGDESCRIPTIONMARGIN = 7;

	public static final Border DIALOGDESCRIPTIONBORDER = BorderFactory.createEmptyBorder(DIALOGDESCRIPTIONMARGIN,
	                                                                                     0,
	                                                                                     DIALOGDESCRIPTIONMARGIN,
	                                                                                     0);
	
}