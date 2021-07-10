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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.TimeZone;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Style;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import de.haukerehfeld.quakeinjector.gui.BrowserLauncher;
import de.haukerehfeld.quakeinjector.gui.ScrollablePanel;

/**
 * the panel that shows Info about the selected map
 */
class PackageDetailPanel extends JPanel implements ChangeListener,
										PackageListSelectionHandler.SelectionListener {
	private static final Dimension DEFAULTIMAGESIZE = new Dimension(200, 150);
	private static final Dimension NOIMAGESIZE = new Dimension(100, 500);

	/**
	 * Currently selected map
	 */
	private Package current = null;

	private JLabel title;
	private JLabel size;
	private JLabel date;

	private ScrollablePanel content;

	private JLabel image;
	private boolean imageDisplayed = false;
	private JPanel imagePanel;

	private JEditorPane description;

	private String screenshotRepositoryPath;
	
	/**
	 * Holds the currently valid screenshot url, for threading reasons
	 */
	private String supposedImageUrl = null;

	/**
	 * @todo 2009-11-05 19:18 hrehfeld     remove, duplicate from packagelistmodel
	 */
		private ImageIcon createImageIcon(String path, String description) {
			java.net.URL imgURL = getClass().getResource(path);
			if (imgURL != null) {
				return new ImageIcon(imgURL, description);
			} else {
				System.err.println("Couldn't find file: " + path);
				return null;
			}
		}
	

	public PackageDetailPanel(String screenshotRepositoryPath) {
		super(new GridBagLayout());
		
		this.screenshotRepositoryPath = screenshotRepositoryPath;

		content = new ScrollablePanel(50, 50) {{
			setLayout(new GridBagLayout());
		}};
		content.setOpaque(false);
		//content.setBackground();
		
		title = new JLabel();
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setOpaque(true);
		title.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		title.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				if (current != null) {
					// TODO: Refactor; get the URL from Configuration
					BrowserLauncher.openURL("https://www.quaddicted.com/reviews/" + current.getId() + ".html");
				}
			}
		});
		
		content.add(title, new GridBagConstraints() {{
			weightx = 1;
			weighty = 0;
			fill = BOTH;
			anchor = PAGE_START;
			ipadx = 5;
			ipady = 20;
		}});

		imagePanel = new JPanel();
		imagePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
 		imagePanel.setOpaque(true);
 		imagePanel.setBackground(java.awt.Color.DARK_GRAY);

		imagePanel.setPreferredSize(DEFAULTIMAGESIZE);
		imagePanel.setMinimumSize(DEFAULTIMAGESIZE);
		//imagePanel.setSize(DEFAULTIMAGESIZE);
 		
 		image = new JLabel();
 		EmptyBorder border = new EmptyBorder(0,0,0,0);
 		image.setBorder(border);
 		image.setHorizontalAlignment(SwingConstants.CENTER);
 		image.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		image.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				// TODO: Refactor
 				BrowserLauncher.openURL(PackageDetailPanel.this.screenshotRepositoryPath + current.getId() + ".jpg");
 			}		
		});
 		imagePanel.add(image);

 		
 		
		description = new JEditorPane("text/html", "");
		description.setEditable(false);
		description.addHyperlinkListener(new HyperlinkListener() {
				@Override public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
						java.net.URL url = e.getURL();
						if (url != null) {
							BrowserLauncher.openURL(url.toString());							
						}
						else {
							System.err.println("Weird hyperlink with null URL: " + e.getDescription());
							String link = "https://www.quaddicted.com/reviews/" + e.getDescription();
							BrowserLauncher.openURL(link);
						}
					}
				}
			});
		content.add(description, new GridBagConstraints() {{
			gridy = 2;
			weightx = 1;
			weighty = 0;
			fill = BOTH;
			anchor = PAGE_START;
		}});

		{
			HTMLEditorKit doc = ((HTMLEditorKit) description.getEditorKit());
			StyleSheet styles = doc.getStyleSheet();
			
			Enumeration rules = styles.getStyleNames();
			while (rules.hasMoreElements()) {
				String name = (String) rules.nextElement();
				Style rule = styles.getStyle(name);
				//System.out.println(rule.toString());
			}
		}

		

		//Put the editor pane in a scroll pane.
		JScrollPane descriptionScroll = new JScrollPane(content);
		descriptionScroll.getViewport().setBackground(javax.swing.UIManager.getColor("TextPane.background"));
		descriptionScroll.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants
		                                             .VERTICAL_SCROLLBAR_ALWAYS);

		add(descriptionScroll, new GridBagConstraints() {{
			gridy = 0;
			gridwidth = 2;
			weightx = 1;
			weighty = 1;
			fill = BOTH;
			anchor = PAGE_START;
		}});

		int detailHeight = 20;
		date = new JLabel();
		date.setHorizontalAlignment(SwingConstants.CENTER);
		add(date, new GridBagConstraints() {{
			gridy = 1;
			gridx = 0;
			weightx = 1;
 			weighty = 0;
			fill = NONE;
			anchor = CENTER;
			ipadx = 5;
			ipady = 3;
		}});

		size = new JLabel();
		size.setHorizontalAlignment(SwingConstants.CENTER);
		add(size, new GridBagConstraints() {{
			gridy = 1;
			gridx = 1;
			weightx = 1;
			weighty = 0;
			fill = NONE;
			anchor = CENTER;
			ipadx = 5;
			ipady = 3;
		}});
	}

	private void addImage() {
		content.add(imagePanel, new GridBagConstraints() {{
			gridy = 1;
			weightx = 1;
			weighty = 1;
			fill = NONE;
			anchor = CENTER;
		}});
		imageDisplayed = true;
	}

	private void removeImage() {
		content.remove(imagePanel);
		imageDisplayed = false;
	}

	private void refreshUi() {
		title.setText(current.getTitle());
		date.setText(toString(current.getDate()));
		size.setText(current.getSize() / 1000f + "mb");

		if (!imageDisplayed) {
			addImage();
		}



		image.setIcon(null);
		
		supposedImageUrl = screenshotRepositoryPath + current.getId() + "_injector.jpg";
		
		//load image in bg thread
		new SwingWorker<ImageIcon,Void>() {
			private final String url = supposedImageUrl;
			
			@Override
			public ImageIcon doInBackground() {
				try {
					return new ImageIcon(new URL(url), current.getId());
				}
				catch (java.net.MalformedURLException e) {
				}
				return null;
			}
			@Override
			public void done() {
				//threading: is the image still valid?
				if (isCancelled() || !supposedImageUrl.equals(url)) {
					return;
				}
				
				ImageIcon icon;
				try {
					icon = get();
				}
				catch (java.lang.InterruptedException e) {
					icon = null;
				}
				catch (java.util.concurrent.ExecutionException e) {
					icon = null;
				}

				if (icon == null || (icon.getImageLoadStatus() & java.awt.MediaTracker.COMPLETE) == 0) {
					removeImage();
					System.err.println("Couldn't load image " + supposedImageUrl);
				}
				else {
					image.setIcon(icon);
					imagePanel.setMinimumSize(DEFAULTIMAGESIZE);
				}
				
				revalidate();
				repaint();
			}
		}.execute();

		description.getEditorKit().createDefaultDocument();
		description.setText(current.getDescription()
		                    + toString(current.getRequirements()) + "<p></p>");
		//scroll to top
		description.setCaretPosition(0);

		revalidate();
		repaint();
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
		System.out.println("StateChanged()");
		refreshUi();
	}
}
