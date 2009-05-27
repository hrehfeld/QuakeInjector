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

public class PackageListModel extends AbstractTableModel implements ChangeListener {
	private static final int name = 0;
	private static final int rating = 1;
	private static final int author = 2;
	private static final int releasedate = 3;
	private static final int installed = 4;
	private static final int requirements = 5;

	private static final String[] columnNames = new String[6];
		static {
			columnNames[name] = "Name";
			columnNames[author] = "Author";
			columnNames[releasedate] = "Releasedate";
			columnNames[installed] = "Installed";
			columnNames[requirements] = "Requirements";
			columnNames[rating] = "Rating";
		}
			
	
	private ChangeListenerList listeners = new ChangeListenerList();
	
	private List<Package> data;


	public PackageListModel() {
		data = new ArrayList<Package>();
	}

	public PackageListModel(List<Package> data) {
		setMapList(data);
	}

	public List<Package> getPackageList() {
		return this.data;
	}


	public void setMapList(List<Package> data) {
		this.data = data;

		for (Package m: data) {
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

	public Object getColumnData(int col, Package info) {
		switch (col) {
		case name: return info.getId();
		case author: return info.getAuthor();
		case releasedate: return info.getDate();
		case installed: return new Boolean(info.isInstalled());
		case rating: return info.getRating().toString();
		case requirements:
			String delimiter = ", ";
			List<Requirement> reqs = new ArrayList<Requirement>(info.getRequirements());
			
			java.util.Collections.sort(reqs);
			String result = Utils.join(reqs, delimiter);
			return result;
			/*
			 * Should never be used
			 */
		default: throw new RuntimeException("This should never happen: check the switch statement above");
		}
	}

    public Class<? extends Object> getColumnClass(int c) {
		switch (c) {
		case name: return String.class;
		case rating: return String.class;
		case author: return String.class;
		case releasedate: return Date.class;
		case installed: return Boolean.class;
		case requirements: return String.class;
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
		listeners.notifyChangeListeners(e.getSource());

		int i = data.indexOf((Package) e.getSource());
		super.fireTableRowsUpdated(i, i);
		
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