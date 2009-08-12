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
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayDeque;
import java.util.Queue;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import de.haukerehfeld.quakeinjector.gui.ProgressPopup;


public class InstallQueuePanel extends JPanel {
	private final static int size = 5;
	private final static int rowHeight = 20;

	private GridBagLayout layout = new GridBagLayout();
	
	private Queue<Job> jobs = new ArrayDeque<Job>();
	
	public InstallQueuePanel() {
		setLayout(layout);
		setPreferredSize(new Dimension(0, size * rowHeight));
	}


	/**
	 * @return PropertyChangeListener that listens on "progress" for the progressbar
	 */
	public Job addJob(String description, ActionListener cancelAction) {
		final JProgressBar progress = new JProgressBar();
		progress.setString(ProgressPopup.progressString(description, 0));
		progress.setValue(0);
		progress.setStringPainted(true);
		add(progress, new GridBagConstraints() {{
			anchor = CENTER;
			gridx = 0;
			fill = BOTH;
			weightx = 1;
		}});

		JButton cancelButton;
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(cancelAction);
		add(cancelButton, new GridBagConstraints() {{
			anchor = CENTER;
			fill = BOTH;
			gridx = 1;
		}});
		


		Job progressListener
			= new Job(progress, cancelButton, description);

		jobs.offer(progressListener);

		replaceComponents();

		return progressListener;
		
	}

	private void replaceComponents() {
		while (jobs.size() > size) {
			Job old = jobs.peek();
			if (old.finished) {
				remove(old.progressBar);
				remove(old.cancelButton);
				jobs.poll();
			}
			else {
				break;
			}
		}

		int row = 0;
		for (Job j: jobs) {
			replaceToRow(j.progressBar, row);
			if (!j.finished) {
				replaceToRow(j.cancelButton, row);
			}
			row++;
		}
		revalidate();
		repaint();
	}

	private void replaceToRow(Component c, int row) {
		GridBagConstraints con = layout.getConstraints(c);
		con.gridy = row;
		layout.setConstraints(c, con);
	}

	private void spanRow(Component c) {
		GridBagConstraints con = layout.getConstraints(c);
		con.gridwidth++;
		layout.setConstraints(c, con);
	}
	
	public void finished(Job j, String message) {
		j.finish(message);

		remove(j.cancelButton);
		spanRow(j.progressBar);
		
		replaceComponents();

	}

	public static class Job implements PropertyChangeListener {
		private JProgressBar progressBar;
		private JButton cancelButton;
		private String description;

		private boolean finished = false;

		public Job(JProgressBar progressBar,
				   JButton cancelButton,
				   String description) {
			this.progressBar = progressBar;
			this.cancelButton = cancelButton;
			this.description = description;
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if ("progress" == evt.getPropertyName()) {
				int p = (Integer) evt.getNewValue();
				progressBar.setString(ProgressPopup.progressString(description, p));
				progressBar.setValue(p);
			} 
		}

		private void finish(String message) {
			finished = true;
			
			progressBar.setEnabled(false);

			progressBar.setString(ProgressPopup.progressString(description, message));
		}
	}
}