package de.haukerehfeld.quakeinjector.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.File;

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