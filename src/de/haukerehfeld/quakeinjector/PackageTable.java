package de.haukerehfeld.quakeinjector;

import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

public class PackageTable extends JTable {

	public PackageTable(PackageListModel maplist) {
		super(maplist);
		
		final TableRowSorter<PackageListModel> sorter = new TableRowSorter<PackageListModel>(maplist);
		setRowSorter(sorter);
		
		setPreferredScrollableViewportSize(new Dimension(500, 500));
		setFillsViewportHeight(true);
		setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
	}

	@SuppressWarnings("unchecked")
	public TableRowSorter<PackageListModel> getRowSorter() {
		return (TableRowSorter<PackageListModel>) super.getRowSorter();
	}
}