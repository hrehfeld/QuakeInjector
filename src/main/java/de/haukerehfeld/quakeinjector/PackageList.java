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
import java.util.Iterator;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Only the Package s in a RequirementsList
 */
public class PackageList implements Iterable<Package>, ChangeListener {
	private List<Package> packages = new ArrayList<Package>();
	private RequirementList requirements;

	public PackageList(RequirementList requirements) {
		this.requirements = requirements;
		setRequirements(requirements);
	}

	private void setRequirements(RequirementList requirements) {
		
		packages.clear();
		for (Requirement r: requirements) {
			if (r instanceof Package) {
				packages.add((Package) r);
			}
		}

		this.requirements = requirements;
		this.requirements.addChangeListener(this);
		notifyChangeListeners();
	}

	public void sort() {
		java.util.Collections.sort(packages);
	}

	public Package get(int i) {
		return packages.get(i);
	}

	public Package get(String id) {
		Requirement r = requirements.get(id);
		if (!(r instanceof Package)) {
			return null;
		}
		return (Package) r;
	}
	
	
	public Iterator<Package> iterator() {
		return packages.iterator();
	}

	public int size() {
		return packages.size();
	}

	public int indexOf(Package r) {
		return packages.indexOf(r);
	}


	/**
	 * easily have change listeners
	 */
	private ChangeListenerList listeners = new ChangeListenerList();

	public void addChangeListener(ChangeListener l) {
		listeners.addChangeListener(l);
	}
	public void removeChangeListener(ChangeListener l) {
		listeners.removeChangeListener(l);
	}
	private void notifyChangeListeners() {
		listeners.notifyChangeListeners(this);
	}

	public void stateChanged(ChangeEvent e) {
		if (!(e.getSource() instanceof RequirementList)) {
			throw new RuntimeException("getting change events with source not a RequirementList");
		}

		setRequirements((RequirementList) e.getSource());
	}	
	
}