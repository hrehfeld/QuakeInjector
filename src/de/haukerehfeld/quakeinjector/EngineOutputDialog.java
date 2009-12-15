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

import java.util.List;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
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
import de.haukerehfeld.quakeinjector.gui.ScrollablePanel;
import de.haukerehfeld.quakeinjector.gui.OkayCancelApplyPanel;

import javax.swing.JTextArea;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.swing.SwingWorker;

import java.awt.Rectangle;

public class EngineOutputDialog extends JDialog {
	private final static String windowTitle = "Engine Output";

	private final InputStream engineOut;

	private final JTextArea output;

	public EngineOutputDialog(final JFrame parent, final InputStream engineOut) {
		super(parent, windowTitle, true);
		this.engineOut = engineOut;

		output = new JTextArea(30, 80);
		output.setLineWrap(true);

		JScrollPane panelScroll = new JScrollPane(output);
		panelScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		panelScroll.getViewport().setBackground(javax.swing.UIManager.getColor("TextPane.background"));
		add(panelScroll, BorderLayout.CENTER);

		final JButton close = new JButton("Close");

		close.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					dispose();
				}
			});

		add(new OkayCancelApplyPanel(close, null, null, false), BorderLayout.PAGE_END);


		new SwingWorker<Void,Void>() {
			@Override
			public Void doInBackground() {
				try {
					InputStreamReader in = new  InputStreamReader(engineOut);
					BufferedReader b = new BufferedReader(in);
					
					String line;
					while ((line = b.readLine()) != null) {
						output.append(line.trim() + "\n");

						//scroll on the event thread
						javax.swing.SwingUtilities.invokeLater(new SwingWorker<Void,Void>() {
						        public Void doInBackground() {
									output.scrollRectToVisible(new Rectangle(0, output.getHeight(), 0, 10));
									return null;
								}
						    });
					}
				}
				catch (java.io.IOException e) {
					output.append("Error: Couldn't read engine output from stream");
				}

				return null;
			}
			@Override
			public void done() {
			}
		}.execute();
	}
}