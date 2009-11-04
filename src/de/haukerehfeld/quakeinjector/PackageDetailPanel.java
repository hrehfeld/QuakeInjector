/*
Copyright 2009 Hauke Rehfeld


This file is part of QuakeInjector.

QuakeInjector is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

QuakeInjector is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with QuakeInjector.  If not, see <http://www.gnu.org/licenses/>.
*/
package de.haukerehfeld.quakeinjector;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.TimeZone;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

import edu.stanford.ejalbert.BrowserLauncher;


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
	private JEditorPane description;

	private BrowserLauncher launcher = null;

	public PackageDetailPanel() {
		super(new GridBagLayout());


		{
			try {
				launcher = new BrowserLauncher();
			}
			catch (Exception e) {
				System.err.println("Couldn't init browserlauncher: " + e.getMessage());
				e.printStackTrace();
			}
		}

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
		description.addHyperlinkListener(new HyperlinkListener() {
				@Override public void hyperlinkUpdate(HyperlinkEvent e) {
					if (launcher != null && e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
						java.net.URL url = e.getURL();
						if (url != null) {
							launcher.openURLinBrowser(url.toString());
						}
						else {
							System.err.println("Weird hyperlink with null URL: " + e.getDescription());
							String link = "http://www.quaddicted.com/reviews/" + e.getDescription();
							launcher.openURLinBrowser(link);
						}
					}
				}
			});

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
	}

	private void refreshUi() {
		title.setText(current.getTitle());
		date.setText(toString(current.getDate()));
		size.setText(current.getSize() / 1000f + "mb");		
		
		description.getEditorKit().createDefaultDocument();
		description.setText("<p align=\"center\">"
		                    + "<img width=\"200\" height=\"150\" "
		                    + "src=\"http://www.quaddicted.com/reviews/screenshots/"
		                    + current.getId() +"_injector.jpg\" /></p><br/>"
		                    + current.getDescription()
		                    + toString(current.getRequirements()));
		description.setCaretPosition(0);
	}

	private String toString(Date date) {
		DateFormat dfm = new SimpleDateFormat("MMM d, yyyy");
		dfm.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
		return dfm.format(date);
	}

	private String toString(List<Requirement> requirements) {
		if (requirements.isEmpty()) {
			return "";
		}
		List<String> links = new ArrayList<String>(requirements.size());
		for (Requirement r: requirements) {
			links.add("<a href=\"" + r.getId() + ".html\">" + r.getId() + "</a>");
		}
		return "<p>Requires: " + Utils.join(links, ", ") + ".</p>";
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