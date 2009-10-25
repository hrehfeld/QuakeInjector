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

import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

import de.haukerehfeld.quakeinjector.packagelist.model.PackageListModel;

/**
 * @todo check if dependency on de.haukerehfeld.quakeinjector.packagelist.model.PackageListModel is necessary
 */
public class PackageTable extends JTable {

	public PackageTable(PackageListModel maplist) {
		super(maplist);
		
		final TableRowSorter<PackageListModel> sorter = new TableRowSorter<PackageListModel>(maplist);
		setRowSorter(sorter);
		
		setPreferredScrollableViewportSize(new Dimension(500, 500));
		setFillsViewportHeight(true);
		setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
	}

	@Override
	@SuppressWarnings("unchecked")
	public TableRowSorter<PackageListModel> getRowSorter() {
		return (TableRowSorter<PackageListModel>) super.getRowSorter();
	}
}