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
package de.haukerehfeld.quakeinjector.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.TimeZone;

import javax.swing.JEditorPane;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.Style;
import java.util.Enumeration;


import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.swing.ImageIcon;
import javax.swing.border.EmptyBorder;
import java.net.URL;

import javax.swing.Scrollable;
import java.awt.Rectangle;


import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

import edu.stanford.ejalbert.BrowserLauncher;

import javax.swing.SwingWorker;
import java.util.concurrent.Future;

/**
 * JPanel that supports Scrollable
 * @see javax.swing.Scrollable
 */
public class ScrollablePanel extends JPanel implements Scrollable {
	private int scrollUnitIncrement;
	private int scrollBlockincrement;
	private boolean tracksWidth;
	private boolean tracksHeight;
	
	public ScrollablePanel(int scrollUnitIncrement,
	                       int scrollBlockincrement,
	                       boolean tracksWidth,
	                       boolean tracksHeight) {
		this.scrollUnitIncrement = scrollUnitIncrement;
		this.scrollBlockincrement = scrollBlockincrement;
		this.tracksWidth = tracksWidth;
		this.tracksHeight = tracksHeight;
	}

	/**
	 * Defaults for viewport size tracking: track width, but not height
	 */
	public ScrollablePanel(int scrollUnitIncrement,
	                       int scrollBlockincrement) {
		this(scrollUnitIncrement, scrollBlockincrement, true, false);
	}
	

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
	                                      int orientation,
	                                      int direction) {
		return scrollUnitIncrement;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
	                                       int orientation,
	                                       int direction) {
		return scrollBlockincrement;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() { return tracksWidth; }
	@Override
	public boolean getScrollableTracksViewportHeight() { return tracksHeight; }
}



