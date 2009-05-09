package de.haukerehfeld.quakeinjector;

import javax.swing.JTable;
import javax.swing.table.TableRowSorter;
import java.awt.Dimension;

public class PackageTable extends JTable {

	public PackageTable(MapList maplist) {
		super(maplist);
		
		final TableRowSorter<MapList> sorter = new TableRowSorter<MapList>(maplist);
		setRowSorter(sorter);
		
		setPreferredScrollableViewportSize(new Dimension(500, 99999));
		setFillsViewportHeight(true);
		setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
	}

	public TableRowSorter<MapList> getRowSorter() {
		return (TableRowSorter<MapList>) super.getRowSorter();
	}
}