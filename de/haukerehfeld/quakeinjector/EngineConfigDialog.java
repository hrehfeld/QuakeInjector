package de.haukerehfeld.quakeinjector;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import de.haukerehfeld.quakeinjector.gui.*;

import java.io.*;

public class EngineConfigDialog extends JDialog {
	private String enginePath;
	private String engineExecutable;
	private String engineCommandline;
	
	public EngineConfigDialog(final JFrame frame,
							  String enginePathDefault,
							  String engineExeDefault,
							  String cmdlineDefault) {
		super(frame, "Engine Configuration", true);

		this.enginePath = enginePathDefault;
		this.engineExecutable = engineExeDefault;
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
		{
			{
				cmdline = new JTextField(cmdlineDefault, 40);
				LabelFieldPanel cmdlinePanel = new LabelFieldPanel("Quake commandline options",
																   cmdline);
				configPanel.add(cmdlinePanel);
			}
		}
		
		final JPathPanel enginePathPanel = new JPathPanel(enginePathDefault,
													 "Path to quake directory",
													 new JPathPanel.PathVerifier.Verifier() {
														 public boolean verify(String path) {
															 File f = new File(path);
															 return (f.exists()
																	 && f.isDirectory()
																	 && f.canRead()
																	 && f.canWrite());
														 }
													 });
		configPanel.add(enginePathPanel);

		final JPathPanel engineExePanel = new JPathPanel(engineExeDefault,
													"Quake Executable",
													 new JPathPanel.PathVerifier.Verifier() {
														 public boolean verify(String f) {
															 File exe = new File(enginePathPanel.getText()
																				 + File.separator
																				 + f);
															 return (exe.exists()
																	 && !exe.isDirectory()
																	 && exe.canRead()
																	 && exe.canExecute());
														 }
													 });
		configPanel.add(engineExePanel);

		
		add(new ClosePanel(this,
						   new ActionListener() {
							   public void actionPerformed(ActionEvent e) {
								   enginePath = enginePathPanel.getText();
								   engineExecutable = engineExePanel.getText();
								   engineCommandline = cmdline.getText();
							  }
						   }),
			BorderLayout.PAGE_END);

		pack();
		setVisible(true);
	}

	public String getEnginePath() {
		return enginePath;
	}
	public String getEngineExecutable() {
		return engineExecutable;
	}
	public String getCommandline() {
		return engineCommandline;
	}

}