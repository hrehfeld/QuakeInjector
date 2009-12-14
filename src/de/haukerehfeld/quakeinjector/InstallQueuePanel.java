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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.Box;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayDeque;
import java.util.Queue;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import javax.swing.Scrollable;
import java.awt.Rectangle;


import de.haukerehfeld.quakeinjector.gui.ProgressPopup;


public class InstallQueuePanel extends JPanel implements Scrollable {
	private final static int size = 5;
	private final static int rowHeight = 20;
	private final static int MARGIN = 3;

	private GridBagLayout layout = new GridBagLayout();
	
	private Queue<Job> jobs = new ArrayDeque<Job>();
	
	public InstallQueuePanel() {
		setLayout(layout);
	}

	/**
	 * @return PropertyChangeListener that listens on "progress" for the progressbar
	 */
	public Job addJob(String description, ActionListener cancelAction) {
		Job progressListener = new Job(cancelAction, description);
		jobs.offer(progressListener);

		layoutComponents();
		
		scrollRectToVisible(new Rectangle(0, getHeight(), 0, 100));
		return progressListener;
	}

	private void layoutComponents() {
		int row = 0;
		for (Job j: jobs) {
			remove(j.progressBar);
			remove(j.cancelButton);
			remove(j.finishedLabel);
			//System.out.println("removing finishedlabel");

			final int row_ = row;
			if (!j.finished) {
				add(j.progressBar, new ProgressBarConstraints() {{ gridy = row_; }});
				add(j.cancelButton, new CancelButtonConstraints() {{ gridy = row_; }});
			}
			else {
				add(j.finishedLabel, new FinishedLabelConstraints() {{ gridy = row_; }});
				//System.out.println("adding finishedlabel");
			}
			row++;
		}

		revalidate();
		repaint();
	}

	public void finished(final Job j, String message) {
		j.finish(message);

		layoutComponents();
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
	                                      int orientation,
	                                      int direction) {
		return rowHeight;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
	                                       int orientation,
	                                       int direction) {
		return rowHeight;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() { return true; }
	@Override
	public boolean getScrollableTracksViewportHeight() { return false; }

	private class RowConstraints extends GridBagConstraints {{
		anchor = CENTER;
		fill = HORIZONTAL;
		insets = new java.awt.Insets(MARGIN, MARGIN, MARGIN, MARGIN);
	}}

	private class ProgressBarConstraints extends RowConstraints {{
		weightx = 1;
		weighty = 1;
	}}

	private class CancelButtonConstraints extends RowConstraints {{
		gridx = 1;
	}}

	private class FinishedLabelConstraints extends RowConstraints {{
		weightx = 1;
		gridwidth = 2;
	}}

	public static class Job implements PropertyChangeListener {
		private JProgressBar progressBar;
		private JButton cancelButton;
		private JLabel finishedLabel;
		
		private String description;

		private boolean finished = false;

		public Job(ActionListener cancelAction, String description) {
			this.description = description;

			progressBar = new JProgressBar();
			progressBar.setString(ProgressPopup.progressString(description, 0));
			progressBar.setValue(0);
			progressBar.setStringPainted(true);
			
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(cancelAction);

			finishedLabel = new JLabel(description);
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if ("progress" == evt.getPropertyName()) {
				int p = (Integer) evt.getNewValue();
				setProgress(p);
			} 
		}

		public void setProgress(int progress) {
			progressBar.setString(ProgressPopup.progressString(description, progress));
			progressBar.setValue(progress);
		}

		private void finish(String message) {
			finished = true;
			progressBar.setEnabled(false);
			cancelButton.setEnabled(false);
			finishedLabel.setText(ProgressPopup.progressString(description, message));
			finishedLabel.setPreferredSize(new Dimension((int) finishedLabel
			                                             .getPreferredSize().getWidth(),
			                                             (int) cancelButton.getSize().getHeight()));
			
		}
	}
}