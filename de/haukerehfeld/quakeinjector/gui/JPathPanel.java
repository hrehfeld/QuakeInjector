package de.haukerehfeld.quakeinjector.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.File;

/**
 * A Panel to input paths
 */
public class JPathPanel extends JPanel {

	private int inputLength = 32;

	private JTextField path;

	public JPathPanel(String defaultPath, String labelText, PathVerifier.Verifier check) {
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		path = new JTextField(defaultPath, inputLength);
		PathVerifier verifier = new PathVerifier(check);
		path.setInputVerifier(verifier);
		path.addActionListener(verifier);
		JLabel label = new JLabel(labelText, SwingConstants.RIGHT);
		
		label.setLabelFor(path);
		add(label);
		add(path);
	}

	public String getText() {
		return path.getText();
	}


	/**
	 * Verify a JTextField for a correct path
	 */
	public static class PathVerifier extends InputVerifier implements ActionListener {
		private Verifier check;

		public PathVerifier(Verifier check) {
			this.check = check;
		}
		
		public boolean shouldYieldFocus(JComponent input) {
			String path = getPath(input);

			if (verify(path)) {
				return true;
			}

			JOptionPane.showMessageDialog(null, //no owner frame
                                          path + " is not a valid directory that I can write to!", //text to display
                                          "Invalid Path", //title
                                          JOptionPane.WARNING_MESSAGE);

            //Reinstall the input verifier.
            input.setInputVerifier(this);

			return false;
		}

		//This method checks input, but should cause no side effects.
		public boolean verify(JComponent input) {
			return verify(getPath(input));
		}

		public String getPath(JComponent input) {
			if (input instanceof JTextField) {
				return ((JTextField) input).getText();
			}
			return null;
		}

		public boolean verify(String path) {
			if (path == null) {
				return false;
			}
			
			return this.check.verify(path);
		}

        public void actionPerformed(ActionEvent e) {
			System.out.println("bla");
            JTextField source = (JTextField)e.getSource();
            shouldYieldFocus(source); //ignore return value
            source.selectAll();
        }

		public interface Verifier {
			public boolean verify(String file);
		}
	}	
}