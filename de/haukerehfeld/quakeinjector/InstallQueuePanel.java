package de.haukerehfeld.quakeinjector;

import java.util.Queue;
import java.util.ArrayDeque;

import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;


public class InstallQueuePanel extends JPanel {
	private final static int size = 5;

	private GridBagLayout layout = new GridBagLayout();
	
	private Queue<Job> jobs = new ArrayDeque<Job>();
	
	public InstallQueuePanel() {
		setLayout(layout);
	}


	/**
	 * @return PropertyChangeListener that listens on "progress" for the progressbar
	 */
	public Job addJob(String description, ActionListener cancelAction) {
		JButton cancelButton;
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(cancelAction);
		add(cancelButton, new GridBagConstraints() {{
			anchor = PAGE_START;
			fill = HORIZONTAL;
			gridx = 0;
		}});
		
		final JProgressBar progress = new JProgressBar();
		progress.setString(progressString(description, 0));
		progress.setValue(0);
		progress.setStringPainted(true);
		add(progress, new GridBagConstraints() {{
			anchor = PAGE_START;
			gridx = 1;
			gridwidth = 1;
			fill = HORIZONTAL;
			weightx = 1;
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

		int row = jobs.size();
		for (Job j: jobs) {
			row--;
			replaceToRow(j.progressBar, row);
			replaceToRow(j.cancelButton, row);
		}
		revalidate();
		repaint();
	}

	private void replaceToRow(Component c, int row) {
		GridBagConstraints con = layout.getConstraints(c);
		con.gridy = row;
		layout.setConstraints(c, con);
	}

	public void finished(Job j) {
		j.finished = true;

		j.progressBar.setEnabled(false);
		j.cancelButton.setEnabled(false);

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
				progressBar.setString(progressString(description, p));
				progressBar.setValue(p);
			} 
		}
	}

	public static String progressString(String description, int progress) {
		return description + ": " + progress + "%";
	}
}