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

import java.util.ArrayList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * Helper class for easy changelistener lists
 */
public class ChangeListenerList {
	private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();

	public void addChangeListener(ChangeListener l) {
		
		if (listeners.indexOf(l) >= 0) {
			return;
		}
		System.out.println(this + ": adding listener " + l + " -- ");
		listeners.add(l);
	}

	public void removeChangeListener(ChangeListener l) {
		listeners.remove(l);
	}
	
	public void notifyChangeListeners(Object source) {
		for (ChangeListener c: listeners) {
			System.out.print(c + ", ");
		}
		System.out.println(";");
		
		ChangeEvent e = new ChangeEvent(source);
		for (ChangeListener l: listeners) {
			System.out.println(this + ": Notifying " + l);
			l.stateChanged(e);
		}
	}
}