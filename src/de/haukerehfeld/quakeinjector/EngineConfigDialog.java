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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.haukerehfeld.quakeinjector.gui.ErrorEvent;
import de.haukerehfeld.quakeinjector.gui.ErrorListener;
import de.haukerehfeld.quakeinjector.gui.JPathPanel;

public class EngineConfigDialog extends JDialog {
	private final static String windowTitle = "Engine Configuration";

	private final ChangeListenerList listeners = new ChangeListenerList();
	private final JPathPanel enginePath;
	private final JPathPanel engineExecutable;
	private final JTextField engineCommandline;

	private final JCheckBox rogue;
	private final JCheckBox hipnotic;
	
	
	public EngineConfigDialog(final JFrame frame,
							  String enginePathDefault,
							  String engineExeDefault,
	                          String cmdlineDefault,
	                          boolean rogueInstalled,
	                          boolean hipnoticInstalled) {
		super(frame, windowTitle, true);

		JLabel description = new JLabel("Configure engine specifics", SwingConstants.CENTER);
		description.setLabelFor(this);
		description.setPreferredSize(new Dimension(100, 50));
		add(description, BorderLayout.PAGE_START);

		JPanel configPanel = new JPanel();
		configPanel.setLayout(new GridBagLayout());
		add(configPanel, BorderLayout.CENTER);

		final JButton okay = new JButton("Save Changes");
		final JButton cancel = new JButton("Cancel");

		class LabelConstraints extends GridBagConstraints {{
				anchor = LINE_START;
				fill = NONE;
				gridx = 0;
				gridwidth = 1;
				gridheight = 1;
				weightx = 0;
				weighty = 0;
				
		}};
		class InputConstraints extends GridBagConstraints {{
				anchor = LINE_END;
				fill = HORIZONTAL;
				gridx = 1;
				gridwidth = 2;
				gridheight = 1;
				weightx = 1;
				weighty = 0;
		}};
		
		{
			JLabel cmdlineLabel = new JLabel("Quake commandline options");
			configPanel.add(cmdlineLabel, new LabelConstraints());
			this.engineCommandline = new JTextField(cmdlineDefault, 40);
			configPanel.add(engineCommandline, new InputConstraints());
		}
		//"Path to quake directory",
		JLabel enginePathLabel = new JLabel("Quake Directory");
		configPanel.add(enginePathLabel, new LabelConstraints() {{ gridy = 1; }});
		enginePath = new JPathPanel(new JPathPanel.Verifier() {
				public boolean verify(File f) {
					return (f.exists()
							&& f.isDirectory()
							&& f.canRead()
							&& f.canWrite());
				}
				public String errorMessage(File f) {
					if (!f.exists()) {
						return "Doesn't exist!";
					}
					else if (!f.isDirectory()) {
						return "Is not a directory!";
					}
					else if (!f.canWrite()) {
						return "Cannot be written to!";
					}
					return null;
				}
			},
			enginePathDefault,
			javax.swing.JFileChooser.DIRECTORIES_ONLY);
		configPanel.add(enginePath, new InputConstraints() {{ gridy = 1; }});
		
		JLabel engineExeLabel = new JLabel("Quake Executable");
		configPanel.add(engineExeLabel, new LabelConstraints() {{ gridy = 2; }});
		engineExecutable = new JPathPanel(
			new JPathPanel.Verifier() {
				public boolean verify(File exe) {
					return (exe.exists()
							&& !exe.isDirectory()
							&& exe.canRead()
							&& exe.canExecute());
				}
				public String errorMessage(File f) {
					if (!f.exists()) {
						return "Doesn't exist!";
					}
					else if (f.isDirectory()) {
						return "Must be an executable file!";
					}
					else if (!f.canExecute()) {
						return "Cannot be executed!";
					}
					return null;
				}
			},
			engineExeDefault,
			new File(enginePathDefault),
			javax.swing.JFileChooser.FILES_ONLY);
		configPanel.add(engineExecutable, new InputConstraints() {{ gridy = 2; }});

		final ChangeListener enableOkay = new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					if (enginePath.verifies() && engineExecutable.verifies()) {
						okay.setEnabled(true);
					}
				}
			};

		{
			enginePath.addErrorListener(new ErrorListener() {
					public void errorOccured(ErrorEvent e) {
						okay.setEnabled(false);
					}
				});
			//change basepath of the exe when quakedir changes
			enginePath.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						engineExecutable.setBasePath(enginePath.getPath());

						enableOkay.stateChanged(e);
					}
				});
		}
		{
			engineExecutable.addErrorListener(new ErrorListener() {
					public void errorOccured(ErrorEvent e) {
						okay.setEnabled(false);
					}
				});
			engineExecutable.addChangeListener(enableOkay);
		}

		enginePath.verify();
		engineExecutable.verify();


		{
			JLabel expansionsInstalled = new JLabel("Expansion packs installed");
			configPanel.add(expansionsInstalled, new LabelConstraints() {{ gridy = 3; }});

			rogue = new JCheckBox("rogue");
			rogue.setMnemonic(KeyEvent.VK_R);
			rogue.setSelected(rogueInstalled);
			configPanel.add(rogue, new InputConstraints() {{ gridy = 3; gridwidth = 1; }});

			hipnotic = new JCheckBox("hipnotic");
			hipnotic.setMnemonic(KeyEvent.VK_H);
			hipnotic.setSelected(hipnoticInstalled);
			configPanel.add(hipnotic, new InputConstraints() {{
				gridy = 3;
				gridx = 2;
				gridwidth = 1;
			}});
		}
		
		

		okay.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					listeners.notifyChangeListeners(this);

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
			JPanel okayCancelPanel = new JPanel();
			okayCancelPanel.setLayout(new BoxLayout(okayCancelPanel, BoxLayout.LINE_AXIS));
			okayCancelPanel.add(Box.createHorizontalGlue());
			okayCancelPanel.add(okay);
			okayCancelPanel.add(Box.createRigidArea(new Dimension(10,0)));
			okayCancelPanel.add(cancel);
			okayCancelPanel.add(Box.createHorizontalGlue());
			
			add(okayCancelPanel, BorderLayout.PAGE_END);
		}
		
	}

	public void packAndShow() {
		pack();
		setVisible(true);
	}

	public File getEnginePath() {
		return enginePath.getPath();
	}
	public File getEngineExecutable() {
		return engineExecutable.getPath();
	}
	public String getCommandline() {
		return engineCommandline.getText();
	}

	/**
	 * get hipnoticInstalled
	 */
	public boolean getHipnoticInstalled() { return hipnotic.isSelected(); }

	/**
	 * get rogueInstalled
	 */
	public boolean getRogueInstalled() { return rogue.isSelected(); }
	
	public void addChangeListener(ChangeListener l) {
		listeners.addChangeListener(l);
	}
}