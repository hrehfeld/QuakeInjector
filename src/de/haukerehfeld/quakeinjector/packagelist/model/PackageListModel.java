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
package de.haukerehfeld.quakeinjector.packagelist.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import de.haukerehfeld.quakeinjector.ChangeListenerList;
import de.haukerehfeld.quakeinjector.PackageList;
import de.haukerehfeld.quakeinjector.Package;

public class PackageListModel extends AbstractTableModel implements ChangeListener {
	private ChangeListenerList listeners = new ChangeListenerList();
	
	private PackageList data;


	public PackageListModel(PackageList data) {
		setMapList(data);
	}

	public void size(JTable table) {
		TableColumnModel m = table.getColumnModel();

		m.getColumn(Column.getColumnNumber(Column.NAME)).setPreferredWidth(70);
		m.getColumn(Column.getColumnNumber(Column.TITLE)).setPreferredWidth(150);
		m.getColumn(Column.getColumnNumber(Column.AUTHOR)).setPreferredWidth(100);
		m.getColumn(Column.getColumnNumber(Column.INSTALLED)).setResizable(false);
		m.getColumn(Column.getColumnNumber(Column.INSTALLED)).setMaxWidth(16);
		m.getColumn(Column.getColumnNumber(Column.RATING)).setMaxWidth(50);
		m.getColumn(Column.getColumnNumber(Column.RATING)).setResizable(false);
		m.getColumn(Column.getColumnNumber(Column.RELEASEDATE)).setPreferredWidth(70);
		m.getColumn(Column.getColumnNumber(Column.RELEASEDATE)).setMaxWidth(100);

	}
	
//  	public List<Package> getPackageList() {
//  		return this.data;
//  	}


	public void setMapList(PackageList data) {
		this.data = data;
		data.addChangeListener(this);
		
		for (Package map: data) {
			map.addChangeListener(this);
		}

		fireTableChanged(new TableModelEvent(this));
	}

    public int getColumnCount() {
        return Column.count();
    }

    @Override
	public String getColumnName(int col) {
        return Column.getColumn(col).header;
    }

	/*
	 * data
	 */
	@Override
    public int getRowCount() {
        return data.size();
    }


	public Object getColumnData(int col, Package info) {
		return Column.getColumn(col).getData(info);
	}

    @Override
	public Class<? extends Object> getColumnClass(int c) {
		return Column.getColumn(c).getColumnClass();
    }

	@Override
    public Object getValueAt(int row, int col) {
        return getColumnData(col, data.get(row));
    }


    @Override
	public boolean isCellEditable(int row, int col) {
		return false;
    }

	public Package getPackage(int row) {
		return data.get(row);
	}

	public void addChangeListener(ChangeListener l) {
		listeners.addChangeListener(l);
	}

	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof PackageList) {
			listeners.notifyChangeListeners(e.getSource());
			super.fireTableChanged(new TableModelEvent(this));
			return;
		}
		
		if (e.getSource() instanceof Package) {
			Package r = (Package) e.getSource();
			listeners.notifyChangeListeners(r);
			int i = data.indexOf(r);
			super.fireTableRowsUpdated(i, i);
			return;
		}

		throw new RuntimeException("didn't recognise what changed!");
	}


	/** 
	 * Update the row filter regular expression from the expression in
	 * the text box.
	 */
	public RowFilter<PackageListModel, Integer> filter(final String filterText) {
		final int[] columnsToCheck = { Column.getColumnNumber(Column.NAME),
									   Column.getColumnNumber(Column.AUTHOR),
									   Column.getColumnNumber(Column.RELEASEDATE)
		};

		String[] filterTexts = filterText.split(" ");

		final List<Pattern> patterns = new ArrayList<Pattern>(filterTexts.length);
		for (String filter: filterTexts) {
			try {
				patterns.add(Pattern.compile(".*" + filter + ".*",
											 Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE));
			} catch (java.util.regex.PatternSyntaxException e) {
				continue;
			}
		}

		RowFilter<PackageListModel, Integer> rf = new RowFilter<PackageListModel,Integer>() {
			@Override
			public boolean include(Entry<? extends PackageListModel, ? extends Integer> entry) {
				//match all patters in at least one column
				for (Pattern pattern: patterns) {
					boolean matches = false;
					for (int i: columnsToCheck) {
						if (pattern.matcher(entry.getStringValue(i)).matches()) {
							matches = true;
						}
					}
					if (!matches) {
						return false;
					}
				}
				return true;
			}
		};

		return rf;
	}					
	

//     /*
//      * Don't need to implement this method unless your table's
//      * data can change.
//      */
//     public void setValueAt(Object value, int row, int col) {
//         data[row][col] = value;
//         fireTableCellUpdated(row, col);
//     }{

}