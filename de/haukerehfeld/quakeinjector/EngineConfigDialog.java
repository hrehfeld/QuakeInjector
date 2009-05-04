package de.haukerehfeld.quakeinjector;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import de.haukerehfeld.quakeinjector.gui.*;

import java.io.*;

public class EngineConfigDialog extends JDialog {
	private File enginePath;
	private File engineExecutable;
	private String engineCommandline;
	
	public EngineConfigDialog(final JFrame frame,
							  String enginePathDefault,
							  String engineExeDefault,
							  String cmdlineDefault) {
		super(frame, "Engine Configuration", true);

		this.enginePath = new File(enginePathDefault);
		this.engineExecutable = new File(enginePathDefault + File.separator + engineExeDefault);
		this.engineCommandline = cmdlineDefault;

		JLabel description = new JLabel("Configure engine specifics", SwingConstants.CENTER);
		description.setLabelFor(this);
		description.setPreferredSize(new Dimension(100, 50));
		add(description, BorderLayout.PAGE_START);

		JPanel configPanel = new JPanel();
		configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.PAGE_AXIS));
		add(configPanel, BorderLayout.CENTER);

		final JTextField cmdline;
		//grid panel for input/label
		cmdline = new JTextField(cmdlineDefault, 40);
		LabelFieldPanel cmdlinePanel = new LabelFieldPanel("Quake commandline options",
														   cmdline);
		configPanel.add(cmdlinePanel);

		//"Path to quake directory",
		final JPathPanel enginePathPanel = new JPathPanel(new JPathPanel.Verifier() {
				public boolean verify(File f) {
					return (f.exists()
							&& f.isDirectory()
							&& f.canRead()
							&& f.canWrite());
				}
			},
			enginePathDefault);
		enginePathPanel.addErrorListener(new ErrorListener() {
				public void errorOccured(ErrorEvent e) {
					String msg = ((JPathPanel) e.getSource()).getPath()
						+ " is not a valid directory that I can write to!";
					String title = "Invalid Path";

					warningDialogue(title, msg);
				}
			});
		configPanel.add(enginePathPanel);

		//"Quake Executable",
		final JPathPanel engineExePanel = new JPathPanel(
			new JPathPanel.Verifier() {
				public boolean verify(File exe) {
					return (exe.exists()
							&& !exe.isDirectory()
							&& exe.canRead()
							&& exe.canExecute());
				}
			},
			engineExeDefault,
			new File(enginePathDefault));
				
		engineExePanel.addErrorListener(new ErrorListener() {
				public void errorOccured(ErrorEvent e) {
					String msg = ((JPathPanel) e.getSource()).getPath()
						+ " is not a valid file that I can execute!";
					String title = "Invalid Path";

					warningDialogue(title, msg);

				}
			});
		configPanel.add(engineExePanel);

		
		add(new ClosePanel(this,
						   new ActionListener() {
							   public void actionPerformed(ActionEvent e) {
								   enginePath = enginePathPanel.getPath();
								   engineExecutable = engineExePanel.getPath();
								   engineCommandline = cmdline.getText();
							  }
						   }),
			BorderLayout.PAGE_END);

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
		return enginePath;
	}
	public File getEngineExecutable() {
		return engineExecutable;
	}
	public String getCommandline() {
		return engineCommandline;
	}

}