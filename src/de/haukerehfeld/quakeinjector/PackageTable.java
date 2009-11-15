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
import java.awt.Color;
import java.awt.Component;

import javax.swing.UIManager;

import javax.swing.JTable;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import de.haukerehfeld.quakeinjector.packagelist.model.PackageListModel;

/**
 * @todo check if dependency on de.haukerehfeld.quakeinjector.packagelist.model.PackageListModel is necessary
 */
public class PackageTable extends JTable {
	private static final int ALTERNATING = 10;
	private static final Color NORMALROWCOLOR = UIManager.getColor("Table.background");
	private static final Color ALTERNATINGROWCOLOR = createAlternatingColor(NORMALROWCOLOR, ALTERNATING);
	private static final int CELLPADDING = 2;

	private final EmptyBorder border = new EmptyBorder(0, CELLPADDING, 0, CELLPADDING);
	                                                           

	public PackageTable(PackageListModel maplist) {
		super(maplist);
		
		final TableRowSorter<PackageListModel> sorter = new TableRowSorter<PackageListModel>(maplist);
		setRowSorter(sorter);
		
		setPreferredScrollableViewportSize(new Dimension(500, 500));
		setFillsViewportHeight(true);
		setColumnSelectionAllowed(false);
		//setCellSelectionEnabled(false);
		setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		setShowGrid(false);
		setIntercellSpacing(new Dimension(0, 0));

		setDefaultRenderer(Package.Rating.class, new PackageListModel.RatingRenderer());		
	}
	
    /**
     * Shades alternate rows in different colors.
     */
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		Component c = super.prepareRenderer(renderer, row, column);
        if (isCellSelected(row, column) == false) {
            c.setBackground(colorForRow(row));
            c.setForeground(UIManager.getColor("Table.foreground"));
        } else {
            c.setBackground(UIManager.getColor("Table.selectionBackground"));
            c.setForeground(UIManager.getColor("Table.selectionForeground"));
        }
        //disable cell focus
        if (c instanceof JComponent) {
			if (!(c instanceof PackageListModel.RatingRenderer)) {
				((JComponent) c).setBorder(border);
			}
			else {
				((JComponent) c).setBorder(new EmptyBorder(0, 0, 0, 0));
			}
		}
        return c;
    }	

	/**
     * Returns the appropriate background color for the given row.
     */
    protected Color colorForRow(int row) {
        return (row % 2 == 0) ? NORMALROWCOLOR : ALTERNATINGROWCOLOR;
    }

	@Override
	@SuppressWarnings("unchecked")
	public TableRowSorter<PackageListModel> getRowSorter() {
		return (TableRowSorter<PackageListModel>) super.getRowSorter();
	}

	/**
	 * Calculate a darker or brighter version of a certain color
	 */
	public static Color createAlternatingColor(Color c, int amount) {
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();

		
		return new Color(r > 127 ? r - amount : r + amount,
		                 g > 127 ? g - amount : g + amount,
		                 b > 127 ? b - amount : b + amount);		
	}
}