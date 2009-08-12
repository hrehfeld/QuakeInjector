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

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

class PackageListSelectionHandler implements ListSelectionListener {
	private PackageListModel list;

	private JTable table;

	private ArrayList<SelectionListener> listeners = new ArrayList<SelectionListener>();

	private int lastSelection = -1;

	public PackageListSelectionHandler(PackageListModel list,
	                                   JTable table) {
		this.list = list;
		this.table = table;
	}

	/**
	 * Listen on selection changes
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) { 
		ListSelectionModel lsm = (ListSelectionModel) e.getSource();

		if (!lsm.isSelectionEmpty()) {
			int selection = table.convertRowIndexToModel(getSelection(lsm));
			if (lastSelection == selection) {
				return;
			}
			notifySelectionListeners(list.getPackage(selection));	
			lastSelection = selection;
		}
	}

	/**
	 * Find out what entry was selected
	 */
	public int getSelection(ListSelectionModel lsm) {
		int selection = -1;
		// Find out which indexes are selected.
		int minIndex = lsm.getMinSelectionIndex();
		int maxIndex = lsm.getMaxSelectionIndex();
		for (int i = minIndex; i <= maxIndex; i++) {
			if (lsm.isSelectedIndex(i)) {
				selection = i;
				break;
			}
		}
		return selection;
	}

	public void addSelectionListener(SelectionListener l) {
		listeners.add(l);
	}

	private void notifySelectionListeners(Package selection) {
		for (SelectionListener l: listeners) {
			l.selectionChanged(selection);
		}
	}

	public interface SelectionListener {
		public void selectionChanged(Package selection);
	}
}


