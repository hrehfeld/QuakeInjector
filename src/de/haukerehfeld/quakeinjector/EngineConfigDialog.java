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
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.event.KeyEvent;
import java.io.File;

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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.haukerehfeld.quakeinjector.gui.ErrorEvent;
import de.haukerehfeld.quakeinjector.gui.ErrorListener;
import de.haukerehfeld.quakeinjector.gui.JPathPanel;
import de.haukerehfeld.quakeinjector.gui.LookAndFeelDefaults;
import de.haukerehfeld.quakeinjector.gui.OkayCancelApplyPanel;

public class EngineConfigDialog extends JDialog {
	private final static String windowTitle = "Engine Configuration";

	private final ChangeListenerList listeners = new ChangeListenerList();
	private final JPathPanel enginePath;
	private final JPathPanel engineExecutable;
	private final JTextField engineCommandline;

	private final JPathPanel downloadPath;


	private final JCheckBox rogue;
	private final JCheckBox hipnotic;
	
	
	public EngineConfigDialog(final JFrame frame,
							  Configuration.EnginePath enginePathDefault,
							  Configuration.EngineExecutable engineExeDefault,
	                          Configuration.DownloadPath downloadPathDefault,
	                          Configuration.EngineCommandLine cmdlineDefault,
	                          boolean rogueInstalled,
	                          boolean hipnoticInstalled) {
		super(frame, windowTitle, true);

		JPanel configPanel = new JPanel();
		configPanel.setBorder(LookAndFeelDefaults.PADDINGBORDER);
		configPanel.setLayout(new GridBagLayout());

		JLabel description = new JLabel("Configure engine specific settings");
		description.setLabelFor(this);
		description.setBorder(LookAndFeelDefaults.DIALOGDESCRIPTIONBORDER);
		configPanel.add(description, new GridBagConstraints());

		final JButton okay = new JButton("Okay");
		final JButton cancel = new JButton("Cancel");
		final JButton apply = new JButton("Apply");


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

		int row = 1;

		Border leftBorder = BorderFactory
		    .createEmptyBorder(0, 0, 0, LookAndFeelDefaults.FRAMEPADDING);

		{
			
			JLabel cmdlineLabel = new JLabel("Quake commandline options");
			cmdlineLabel.setBorder(leftBorder);

			this.engineCommandline = new JTextField(cmdlineDefault.get(), 40);

			final int row_ = row;
			configPanel.add(cmdlineLabel, new LabelConstraints() {{ gridy = row_; }});
			configPanel.add(engineCommandline, new InputConstraints() {{ gridy = row_; }});
		}

		++row;

		{
			//"Path to quake directory",
			JLabel enginePathLabel = new JLabel("Quake Directory");
			enginePathLabel.setBorder(leftBorder);
			
			enginePath = new JPathPanel(new JPathPanel.WritableDirectoryVerifier(),
			                            enginePathDefault.get(),
			                            javax.swing.JFileChooser.DIRECTORIES_ONLY);
			final int row_ = row;
			configPanel.add(enginePathLabel, new LabelConstraints() {{ gridy = row_; }});
			configPanel.add(enginePath, new InputConstraints() {{ gridy = row_; }});
		}
		++row;
		
		JLabel engineExeLabel = new JLabel("Quake Executable");
		engineExeLabel.setBorder(leftBorder);
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
			engineExeDefault.get(),
			enginePathDefault.get(),
			javax.swing.JFileChooser.FILES_ONLY);

		{
			final int row_ = row;
			configPanel.add(engineExeLabel, new LabelConstraints() {{ gridy = row_; }});
			configPanel.add(engineExecutable, new InputConstraints() {{ gridy = row_; }});
		}


		{
		}
		{
		}

		enginePath.verify();
		engineExecutable.verify();

		++row;

		{
			//"Path to quake directory",
			JLabel downloadLabel = new JLabel("Download Directory");
			downloadLabel.setBorder(leftBorder);
			downloadPath = new JPathPanel(new JPathPanel.WritableDirectoryVerifier(),
			                              downloadPathDefault.get(),
			                              javax.swing.JFileChooser.DIRECTORIES_ONLY);
			downloadPath.verify();

			final int row_ = row;
			configPanel.add(downloadLabel, new LabelConstraints() {{ gridy = row_; }});
			configPanel.add(downloadPath, new InputConstraints() {{ gridy = row_; }});
			
		}
		++row;


		{
			JLabel expansionsInstalled = new JLabel("Expansion packs installed");
			expansionsInstalled.setBorder(leftBorder);

			rogue = new JCheckBox("rogue");
			rogue.setMnemonic(KeyEvent.VK_R);
			rogue.setSelected(rogueInstalled);

			hipnotic = new JCheckBox("hipnotic");
			hipnotic.setMnemonic(KeyEvent.VK_H);
			hipnotic.setSelected(hipnoticInstalled);

			final int row_ = row;
			configPanel.add(expansionsInstalled, new LabelConstraints() {{ gridy = row_; }});
			configPanel.add(rogue, new InputConstraints() {{ gridy = row_; gridwidth = 1; }});
			configPanel.add(hipnotic, new InputConstraints() {{
				gridy = row_;
				gridx = 2;
				gridwidth = 1;
			}});
		}

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setBorder(LookAndFeelDefaults.PADDINGBORDER);
		tabbedPane.addTab("Engine Specifics", null, configPanel, "Configure Engine Specifics");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		add(tabbedPane, BorderLayout.CENTER);
		

		class EnableOkay implements ChangeListener, DocumentListener {
			@Override
			public void changedUpdate(DocumentEvent e) {
				check();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				check();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				check();
			}

			@Override
			public void stateChanged(ChangeEvent e) {
				check();
			}
			
			private void check() {
				if (enginePath.verifies() && engineExecutable.verifies()) {
					okay.setEnabled(true);
					apply.setEnabled(true);
				}
			}
		};
		
		final EnableOkay enableOkay = new EnableOkay() ;
		

		engineCommandline.getDocument().addDocumentListener(enableOkay);
		
		enginePath.addErrorListener(new ErrorListener() {
				public void errorOccured(ErrorEvent e) {
					okay.setEnabled(false);
					apply.setEnabled(false);
				}
			});
		//change basepath of the exe when quakedir changes
		enginePath.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					engineExecutable.setBasePath(enginePath.getPath());
				}
			});
		enginePath.addChangeListener(enableOkay);

		engineExecutable.addErrorListener(new ErrorListener() {
				public void errorOccured(ErrorEvent e) {
					okay.setEnabled(false);
					apply.setEnabled(false);
				}
			});
		engineExecutable.addChangeListener(enableOkay);

		downloadPath.addChangeListener(enableOkay);

		rogue.addChangeListener(enableOkay);
		hipnotic.addChangeListener(enableOkay);

		
		

		ActionListener save = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					listeners.notifyChangeListeners(this);
					apply.setEnabled(false);
				}
			};
		

		okay.addActionListener(save);
		apply.addActionListener(save);

		ActionListener close = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					dispose();
				}
			};

		okay.addActionListener(close);
		cancel.addActionListener(close);

		{
			JPanel okayCancelPanel = new OkayCancelApplyPanel(okay, cancel, apply, true);
			add(okayCancelPanel, BorderLayout.PAGE_END);
		}
		
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

	public File getDownloadPath() {
		return downloadPath.getPath();
	}
	
	public void addChangeListener(ChangeListener l) {
		listeners.addChangeListener(l);
	}
}