package de.haukerehfeld.quakeinjector.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.haukerehfeld.quakeinjector.ChangeListenerList;
import de.haukerehfeld.quakeinjector.RelativePath;

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
	private final JButton fileChooserButton;
	private final JFileChooser chooser;


	private final Verifier check;

	/**
	 * @param filesAndOrDirectories what kind of files can be selected with the filechooser:
	 *        one of JFileChooser.DIRECTORIES_ONLY,
	 *               JFileChooser.FILES_AND_DIRECTORIES,
	 *               JFileChooser.FILES_ONLY
	 */
	public JPathPanel(Verifier check, String defaultPath, int filesAndOrDirectories) {
		this(check, defaultPath, null, filesAndOrDirectories);
	}

	/**
	 * @param filesAndOrDirectories what kind of files can be selected with the filechooser:
	 *        one of JFileChooser.DIRECTORIES_ONLY,
	 *               JFileChooser.FILES_AND_DIRECTORIES,
	 *               JFileChooser.FILES_ONLY
	 */
	public JPathPanel(Verifier check,
					  String defaultPath,
					  File basePath,
					  int filesAndOrDirectories) {
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

		this.chooser = new JFileChooser(getPath());
		chooser.setFileSelectionMode(filesAndOrDirectories);
		
		this.fileChooserButton = new JButton("Select");
        fileChooserButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int returnVal = chooser.showOpenDialog(JPathPanel.this);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = chooser.getSelectedFile();

						setPath(file);
					}
				}
			});
		add(fileChooserButton);
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
			chooser.setCurrentDirectory(getPath());
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
		File oldFile = getPath();
		boolean verifies = verifies();
		this.basePath = basePath;
		if (verifies) {
			this.path.setText(RelativePath.getRelativePath(basePath, oldFile));
		}
		//put this above the if (verifies) to change chooser to the new basedir
		this.chooser.setCurrentDirectory(getPath());
		verify();
	}

	public void setPath(String path) {
		setPath(new File(path));
	}
	
	public void setPath(File path) {
		String pathString;
		if (basePath != null) {
			pathString = RelativePath.getRelativePath(basePath, path);
		}
		else {
			pathString = path.getAbsolutePath();
		}

		this.path.setText(pathString);
	}

	/**
	 * get a file representing what this pathpanel is pointing to
	 */
	public File getPath() {
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