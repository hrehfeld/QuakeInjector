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
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.RowFilter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.JTable;

public class PackageListModel extends AbstractTableModel implements ChangeListener {
	private static final int columnCount = 6;
	
	private static final int name = 0;
	private static final int title = 1;
	private static final int author = 2;
	private static final int releasedate = 3;
	private static final int rating = 4;
	private static final int installed = 5;

	private static final String nameHeader = "Name";
	private static final String titleHeader = "Title";
	private static final String authorHeader = "Author";
	private static final String releasedateHeader = "Released";
	private static final String ratingHeader = "Rating";
	private static final String installedHeader = "";

	private static final String[] columnNames = new String[columnCount];
	                                              
	static {
		columnNames[name] = nameHeader;
		columnNames[title] = titleHeader;
		columnNames[author] = authorHeader;
		columnNames[releasedate] = releasedateHeader;
		columnNames[installed] = installedHeader;
		columnNames[rating] = ratingHeader;
	}
			
	
	private ChangeListenerList listeners = new ChangeListenerList();
	
	private PackageList data;


	public PackageListModel(PackageList data) {
		setMapList(data);
	}

	public void size(JTable table) {
		TableColumnModel m = table.getColumnModel();
		
		m.getColumn(name).setPreferredWidth(70);
		m.getColumn(title).setPreferredWidth(150);
		m.getColumn(author).setPreferredWidth(100);
		m.getColumn(installed).setResizable(false);
		m.getColumn(installed).setMaxWidth(16);
		m.getColumn(rating).setMaxWidth(50);
		m.getColumn(rating).setResizable(false);
		m.getColumn(releasedate).setPreferredWidth(70);
		m.getColumn(releasedate).setMaxWidth(100);

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
        return columnCount;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

	/*
	 * data
	 */
	
    public int getRowCount() {
        return data.size();
    }

	public Object getColumnData(int col, Package info) {
		if (info instanceof Package) {
			Package p = (Package) info;
			switch (col) {
			case name: return p.getId();
			case author: return p.getAuthor();
			case title: return p.getTitle();
			case releasedate: return p.getDate();
			case installed: return new Boolean(p.isInstalled());
			case rating: return p.getRating().toString();
			default: throw new RuntimeException("This should never happen: check the switch statement above");
			}
		}

		switch (col) {
		case name: return info.getId();
		case author: return "";
		case title: return "";
		case releasedate: return null;
		case installed: return false;
		case rating: return "";
		default: throw new RuntimeException("This should never happen: check the switch statement above");
		}
		
		
	}

    public Class<? extends Object> getColumnClass(int c) {
		switch (c) {
		case name: return String.class;
		case rating: return String.class;
		case title: return String.class;
		case author: return String.class;
		case releasedate: return Date.class;
		case installed: return Boolean.class;
			/*
			 * Should never be used
			 */
		default: throw new RuntimeException("This should never happen: check the switch statement above");
		}
    }

    public Object getValueAt(int row, int col) {
        return getColumnData(col, data.get(row));
    }


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
		final int[] columnsToCheck = { name, author, releasedate };

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