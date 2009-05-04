package de.haukerehfeld.quakeinjector.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.io.File;
import java.util.ArrayList;

import de.haukerehfeld.quakeinjector.ChangeListenerList;

/**
 * A Panel to input paths
 */
public class JPathPanel extends JPanel {
	private ArrayList<ErrorListener> errorListeners = new ArrayList<ErrorListener>();

	private ChangeListenerList changeListeners = new ChangeListenerList();

	private int inputLength = 32;

	private JTextField path;

	private final File basePath;

	private Verifier check;

	public JPathPanel(Verifier check, String defaultPath) {
		this(check, defaultPath, null);
	}
	
	public JPathPanel(Verifier check,
					  String defaultPath,
					  File basePath) {
		this.check = check;
		this.basePath = basePath;
		
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		this.path = new JTextField(defaultPath, inputLength);
		PathVerifier verifier = new PathVerifier();
		path.setInputVerifier(verifier);
		path.addActionListener(verifier);
		add(path);

		
	}

	public File getPath() {
		return getPath(this.path.getText());
	}
	
	private File getPath(String path) {
		File file;
		if (basePath != null) {
			file = new File(basePath.getAbsolutePath() + File.separator + path);
		}
		else {
			file = new File(path);
		}
		return file;
	}

	private boolean verifyBla(String path) {
		if (path == null) {
			return false;
		}

		return this.check.verify(getPath(path));
	}

	private String getPathString(JComponent input) {
		if (!(input instanceof JTextField)) {
			return null;
		}
		return ((JTextField) input).getText();
	}

	public void addErrorListener(ErrorListener e) {
		errorListeners.add(e);
	}

	private void notifyErrorListeners() {
		ErrorEvent e = new SimpleErrorEvent(this);
		for (ErrorListener l: errorListeners) {
			l.errorOccured(e);
		}
	}

	public void addChangeListener(ChangeListener l) {
		changeListeners.addChangeListener(l);
	}

	private void notifyChangeListeners() {
		changeListeners.notifyChangeListeners(this);
	}

	public interface Verifier {
		public boolean verify(File file);
	}


	private class PathVerifier extends InputVerifier implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JTextField source = (JTextField)e.getSource();
			shouldYieldFocus(source);
			source.selectAll();
		}

		@Override
		public boolean verify(JComponent input) {
			return verifyBla(getPathString(input));
		}

		@Override
		public boolean shouldYieldFocus(JComponent input) {
			String path = getPathString(input);

			if (verifyBla(path)) {
				notifyChangeListeners();
				return true;
			}

			notifyErrorListeners();

			//Reinstall the input verifier.
			input.setInputVerifier(this);

			return false;
		}
	}
}