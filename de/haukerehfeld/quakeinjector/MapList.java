package de.haukerehfeld.quakeinjector;

import javax.swing.JTable;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import javax.swing.table.AbstractTableModel;

import java.util.GregorianCalendar;

import java.util.*;
import java.util.regex.Pattern;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;

public class MapList extends AbstractTableModel implements ChangeListener {
	private static final int name = 0;
	private static final int title = 1;
	private static final int author = 2;
	private static final int releasedate = 3;
	private static final int installed = 4;
	private static final int requirements = 5;
	
	private String[] columnNames = {"Name",
									"Title",
									"Author",
									"Releasedate",
									"Installed",
									"Requirements"};

	private ChangeListenerList listeners = new ChangeListenerList();
	
	private List<MapInfo> data;


	public MapList() {
		data = new ArrayList<MapInfo>();
	}

	public MapList(List<MapInfo> data) {
		setMapList(data);
	}


	public void setMapList(List<MapInfo> data) {
		this.data = data;

		for (MapInfo m: data) {
			m.addChangeListener(this);
		}

		fireTableChanged(new TableModelEvent(this));
	}

    public int getColumnCount() {
        return columnNames.length;
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

	public Object getColumnData(int col, MapInfo info) {
		switch (col) {
		case name: return info.getId();
		case title: return info.getTitle();
		case author: return info.getAuthor();
		case releasedate: return info.getDate();
		case installed: return new Boolean(info.isInstalled());
		case requirements:
			String result = "";
			for (MapInfo m: info.getRequirements()) {
				result += m.getId() + ", ";
			}
			return result;
			/*
			 * Should never be used
			 */
		default: throw new RuntimeException("This should never happen: check the switch statement above");
		}
	}

    public Object getValueAt(int row, int col) {
        return getColumnData(col, data.get(row));
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
		return false;
    }

	public MapInfo getMapInfo(int row) {
		return data.get(row);
	}

	public void addChangeListener(ChangeListener l) {
		listeners.addChangeListener(l);
	}

	public void stateChanged(ChangeEvent e) {
		listeners.notifyChangeListeners(e.getSource());

		int i = data.indexOf((MapInfo) e.getSource());
		super.fireTableRowsUpdated(i, i);
		
	}


	/** 
	 * Update the row filter regular expression from the expression in
	 * the text box.
	 */
	public void filter(TableRowSorter<MapList> sorter, final String filterText) {
		//If current expression doesn't parse, don't update.
		final Pattern pattern;
		try {
			pattern = Pattern.compile(".*" + filterText + ".*",
									  Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE);
		} catch (java.util.regex.PatternSyntaxException e) {
			return;
		}

		RowFilter<MapList, Integer> rf = new RowFilter<MapList,Integer>() {
			public boolean include(Entry<? extends MapList, ? extends Integer> entry) {
				int[] columnsToCheck = { name, title, author };
				
				for (int i: columnsToCheck) {
					if (pattern.matcher(entry.getStringValue(i)).matches()) {
						return true;
					}
				}
				return false;
			}
		};
		sorter.setRowFilter(rf);
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