package de.haukerehfeld.quakeinjector;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.haukerehfeld.quakeinjector.gui.*;

import java.io.*;

public class EngineConfigDialog extends JDialog {
	private final static String windowTitle = "Engine Configuration";

	private final ChangeListenerList listeners = new ChangeListenerList();
	private final JPathPanel enginePath;
	private final JPathPanel engineExecutable;
	private final JTextField engineCommandline;
	
	public EngineConfigDialog(final JFrame frame,
							  String enginePathDefault,
							  String engineExeDefault,
							  String cmdlineDefault) {
		super(frame, windowTitle, true);

		JLabel description = new JLabel("Configure engine specifics", SwingConstants.CENTER);
		description.setLabelFor(this);
		description.setPreferredSize(new Dimension(100, 50));
		add(description, BorderLayout.PAGE_START);

		JPanel configPanel = new JPanel();
		configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.PAGE_AXIS));
		add(configPanel, BorderLayout.CENTER);

		final JButton okay = new JButton("Save Changes");
		final JButton cancel = new JButton("Cancel");

		{
			this.engineCommandline = new JTextField(cmdlineDefault, 40);
			LabelFieldPanel cmdlinePanel = new LabelFieldPanel("Quake commandline options",
															   engineCommandline);
			configPanel.add(cmdlinePanel);
		}
		//"Path to quake directory",
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
		configPanel.add(enginePath);
		
		//"Quake Executable",
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
		configPanel.add(engineExecutable);

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
		configPanel.add(okay);
		configPanel.add(cancel);
	}

	public void packAndShow() {
		pack();
		setVisible(true);
	}

	private void warningDialogue(String title, String msg) {
		JOptionPane.showMessageDialog(this, //no owner frame
									  msg,
									  title,
									  JOptionPane.WARNING_MESSAGE);
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

	public void addChangeListener(ChangeListener l) {
		listeners.addChangeListener(l);
	}
}