package de.haukerehfeld.quakeinjector.gui;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * A Panel with a label and a field
 */
public class LabelFieldPanel extends JPanel {
	public LabelFieldPanel(String labelText, JComponent field) {
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		JLabel label = new JLabel(labelText, SwingConstants.RIGHT);
		
		label.setLabelFor(field);
		add(label);
		add(field);
	}
}