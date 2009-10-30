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

import java.util.EnumSet;
import java.util.Date;

import de.haukerehfeld.quakeinjector.Package;

/**
 * Enum for the table columns. Saves all info about them.
 */
public enum Column {
	NAME("Name") {
		public Object getData(Package p) {
			return p.getId();
		}

		public Class<? extends Object> getColumnClass() { return String.class; }
	},
		TITLE("Title") {
		public Object getData(Package p) {
			return p.getTitle();
		}
		public Class<? extends Object> getColumnClass() { return String.class; }
	},
		AUTHOR("Author") {
		public Object getData(Package p) {
			return p.getAuthor();
		}
		public Class<? extends Object> getColumnClass() { return String.class; }
	},
		RELEASEDATE("Released") {
		public Object getData(Package p) {
			return p.getDate();
		}
		public Class<? extends Object> getColumnClass() { return Date.class; }
	},
		RATING("Rating") {
		public Object getData(Package p) {
			return p.getRating();
		}
		public Class<? extends Object> getColumnClass() { return Package.Rating.class; }
	},
		INSTALLED("") {
		public Object getData(Package p) {
			return new Boolean(p.isInstalled());
		}
		public Class<? extends Object> getColumnClass() { return Boolean.class; }
	}
		;


	public String header;

	private Column(String header) {
		this.header = header;
	}

	public abstract Object getData(Package p);

	public abstract Class<? extends Object> getColumnClass();

	public static int getColumnNumber(Column c) {
		return java.util.Arrays.binarySearch(values(), c);
	}

	public static Column getColumn(int column) {
		Column[] values = values();
		if (values.length <= column) {
			throw new RuntimeException("Unknown Column");
		}
		return values[column];
	}

	public static int count() {
		return values().length;
	}
}

	
