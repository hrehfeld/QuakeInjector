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
	private final ArrayList<ErrorListener> errorListeners = new ArrayList<ErrorListener>();
	private final ChangeListenerList changeListeners = new ChangeListenerList();

	private final int inputLength = 32;

	private File basePath;

	private final JTextField path;
	private final JLabel errorLabel;


	private final Verifier check;

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
		path.getDocument().addDocumentListener(verifier);
		add(path);

		this.errorLabel = new JLabel();
		add(errorLabel);
	}

	/**
	 * Just checks if the current path is valid without notifying listeners
	 */
	public boolean verifies() {
		return check();
	}

	/**
	 * Check if current path is valid and notify listeners
	 */
	public boolean verify() {
		if (!check()) {
			notifyErrorListeners();
			return false;
		}
		else {
			notifyChangeListeners();
			return true;
		}
	}


	private boolean check() {
		File f = getPath();
		errorLabel.setText(check.errorMessage(f));
		return this.check.verify(f);
	}
	
	public void setBasePath(File basePath) {
		this.basePath = basePath;
		verify();
	}

	/**
	 * get a file representing what this pathpanel is pointing to
	 */
	public File getPath() {
		if (this.path == null) {
			System.out.println("wtf");
		}
		/*
		 * Build a file object from - if set - the basepath and the textfield content
		 */
		String path = this.path.getText();
		if (path == null) {
			path = "";
		}

		File file;
		if (basePath != null) {
			file = new File(basePath.getAbsolutePath() + File.separator + path);
		}
		else {
			file = new File(path);
		}
		return file;
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

	/**
	 * Hack: Because i can't call verify() from the inner class that has @Override verify(Stuff s);
	 */
	private boolean verify_() {
		return verify();
	}
	
	

	public interface Verifier {
		public boolean verify(File file);
		public String errorMessage(File file);
	}


	private class PathVerifier extends InputVerifier implements DocumentListener {
		@Override
		public void insertUpdate(DocumentEvent e) {
			verify_();
		}
		@Override
		public void removeUpdate(DocumentEvent e) {
			verify_();
		}
		@Override
		public void changedUpdate(DocumentEvent e) {
			verify_();
		}
		@Override
		public boolean verify(JComponent input) {
			return verify_();
		}
		@Override
		public boolean shouldYieldFocus(JComponent input) {
			verify_();
			return true;
		}
	}
}