package de.haukerehfeld.quakeinjector;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.util.List;

/**
 * the panel that shows Info about the selected map
 */
class PackageDetailPanel extends JPanel implements ChangeListener,
										PackageListSelectionHandler.SelectionListener {
	/**
	 * Currently selected map
	 */
	private Package current = null;

	private JLabel title;
	private JLabel size;
	private JLabel date;
	private JLabel requirements;

	private JEditorPane description;

	public PackageDetailPanel() {
		super(new GridBagLayout());

		title = new JLabel();
// 		panelSize = new Dimension(getSize());
// 		panelSize.setSize(panelSize.getWidth(), 50);
		title.setPreferredSize(new Dimension((int) getSize().getWidth(), 30));
		title.setHorizontalAlignment(SwingConstants.CENTER);
 		add(title, new GridBagConstraints() {{
			gridwidth = 2;
			weightx = 0;
			weighty = 0;
			fill = BOTH;
			anchor = LINE_START;
		}});

		description = new JEditorPane("text/html", "");
		description.setEditable(false);

		//Put the editor pane in a scroll pane.
		JScrollPane descriptionScroll = new JScrollPane(description);
		descriptionScroll.setPreferredSize(new Dimension((int) getSize().getWidth(), 145));
		descriptionScroll.setMinimumSize(new Dimension(10, 10));		

		add(descriptionScroll, new GridBagConstraints() {{
			gridy = 1;
			gridwidth = 2;
			weightx = 1;
			weighty = 1;
			fill = BOTH;
			anchor = LINE_END;
		}});

		int detailHeight = 20;
		date = new JLabel();
		date.setHorizontalAlignment(SwingConstants.TRAILING);
		date.setPreferredSize(new Dimension((int) getSize().getWidth(), detailHeight));
		add(date, new GridBagConstraints() {{
			gridy = 2;
			gridx = 0;
			weightx = 1;
 			weighty = 0;
			fill = BOTH;
			anchor = LINE_START;
		}});

		size = new JLabel();
		size.setHorizontalAlignment(SwingConstants.CENTER);
		date.setPreferredSize(new Dimension((int) getSize().getWidth(), detailHeight));		
		add(size, new GridBagConstraints() {{
			gridy = 2;
			gridx = 1;
			weightx = 1;
			weighty = 0;
			fill = BOTH;
			anchor = LINE_END;
		}});


		requirements = new JLabel();
// 		panelSize = new Dimension(getSize());
// 		panelSize.setSize(panelSize.getWidth(), 50);
		requirements.setPreferredSize(new Dimension((int) getSize().getWidth(), detailHeight));
		requirements.setHorizontalAlignment(SwingConstants.LEADING);
 		add(requirements, new GridBagConstraints() {{
			gridy = 3;
			gridwidth = 2;
			weightx = 0;
			weighty = 0;
			fill = BOTH;
			anchor = LINE_START;
		}});
	}

	private void refreshUi() {
		title.setText(current.getTitle());
		date.setText(toString(current.getDate()));
		size.setText((float) current.getSize() / 1000f + "mb");		
		
		requirements.setText(toString(current.getRequirements()));

		description.getEditorKit().createDefaultDocument();
		description.setText("<p align=\"center\">"
		                    + "<img width=\"200\" height=\"150\" "
		                    + "src=\"http://www.quaddicted.com/reviews/screenshots/"
		                    + current.getId() +"_injector.jpg\" /></p><br/>"
		                    + current.getDescription());
		description.setCaretPosition(0);
	}

	private String toString(Date date) {
		DateFormat dfm = new SimpleDateFormat("MMM d, yyyy");
		dfm.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
		return dfm.format(date);
	}

	private String toString(List<Requirement> requirements) {
		if (requirements.isEmpty()) {
			return "No Requirements.";
		}
		return "Requires: " + Utils.join(requirements, ", ") + ".";
	}

	@Override
	public void selectionChanged(Package map) {
		this.current = map;

		refreshUi();

	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		refreshUi();
	}
	
}