package de.haukerehfeld.quakeinjector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.util.ArrayList;

class PackageListSelectionHandler implements ListSelectionListener {
	private PackageListModel list;

	private JTable table;

	private ArrayList<SelectionListener> listeners = new ArrayList<SelectionListener>();

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
			notifySelectionListeners(list.getPackage(selection));	
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


