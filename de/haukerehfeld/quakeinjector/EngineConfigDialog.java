package de.haukerehfeld.quakeinjector;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
		configPanel.setLayout(new GridBagLayout());
		add(configPanel, BorderLayout.CENTER);

		final JButton okay = new JButton("Save Changes");
		final JButton cancel = new JButton("Cancel");

		{
			JLabel cmdlineLabel = new JLabel("Quake commandline options");
			configPanel.add(cmdlineLabel, new GridBagConstraints() {{
				anchor = LINE_START;
				fill = NONE;
				gridx = 0;
				gridy = 0;
				gridwidth = 1;
				gridheight = 1;
				weightx = 0;
				weighty = 0;
				
			}});
			this.engineCommandline = new JTextField(cmdlineDefault, 40);
			configPanel.add(engineCommandline, new GridBagConstraints() {{
				anchor = LINE_END;
				fill = HORIZONTAL;
				gridx = 1;
				gridy = 0;
				gridwidth = 1;
				gridheight = 1;
				weightx = 1;
				weighty = 0;
			}});
		}
		//"Path to quake directory",
		JLabel enginePathLabel = new JLabel("Quake Directory");
		configPanel.add(enginePathLabel, new GridBagConstraints() {{
			anchor = LINE_START;
			fill = NONE;
			gridx = 0;
			gridy = 1;
			gridwidth = 1;
			gridheight = 1;
			weightx = 0;
			weighty = 0;
				
		}});
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
		configPanel.add(enginePath, new GridBagConstraints() {{
			anchor = LINE_END;
			fill = HORIZONTAL;
			gridx = 1;
			gridy = 1;
			gridwidth = 1;
			gridheight = 1;
			weightx = 1;
			weighty = 0;
		}});
		
		//"Quake Executable",
		JLabel engineExeLabel = new JLabel("Quake Executable");
		configPanel.add(engineExeLabel, new GridBagConstraints() {{
			anchor = LINE_START;
			fill = NONE;
			gridx = 0;
			gridy = 2;
			gridwidth = 1;
			gridheight = 1;
			weightx = 0;
			weighty = 0;
				
		}});
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
		configPanel.add(engineExecutable, new GridBagConstraints() {{
			anchor = LINE_END;
			fill = HORIZONTAL;
			gridx = 1;
			gridy = 2;
			gridwidth = 1;
			gridheight = 1;
			weightx = 1;
			weighty = 0;
		}});

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