package de.haukerehfeld.quakeinjector;

import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

public class PackageTable extends JTable {

	public PackageTable(PackageList maplist) {
		super(maplist);
		
		final TableRowSorter<PackageList> sorter = new TableRowSorter<PackageList>(maplist);
		setRowSorter(sorter);
		
		setPreferredScrollableViewportSize(new Dimension(500, 500));
		setFillsViewportHeight(true);
		setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
	}

	@SuppressWarnings("unchecked")
	public TableRowSorter<PackageList> getRowSorter() {
		return (TableRowSorter<PackageList>) super.getRowSorter();
	}
}