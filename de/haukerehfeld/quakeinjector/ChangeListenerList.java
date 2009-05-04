package de.haukerehfeld.quakeinjector;

import java.util.ArrayList;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;


/**
 * Helper class for easy changelistener lists
 */
public class ChangeListenerList {
	private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();

	public void addChangeListener(ChangeListener l) {
		listeners.add(l);
	}

	public void notifyChangeListeners(Object source) {
		ChangeEvent e = new ChangeEvent(source);
		for (ChangeListener l: listeners) {
			l.stateChanged(e);
		}
	}
}