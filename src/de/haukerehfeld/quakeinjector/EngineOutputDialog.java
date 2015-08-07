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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import de.haukerehfeld.quakeinjector.gui.OkayCancelApplyPanel;

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

					
					output.append("Starting engine...\n");
					String line;
					int lineCount = 0;
					while ((line = b.readLine()) != null) {
						//append is thread safe
						output.append(line.trim() + "\n");
						lineCount++;

						//scroll on the event thread
						javax.swing.SwingUtilities.invokeLater(new SwingWorker<Void,Void>() {
						        public Void doInBackground() {
									output.scrollRectToVisible(new Rectangle(0, output.getHeight(), 0, 10));
									return null;
								}
						    });
					}
					if (lineCount == 0) {
						output.append("Done with no output.\n");
					}
					else {
						output.append("Done.\n");
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