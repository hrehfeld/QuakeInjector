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

import java.util.Iterator;
import java.util.TreeSet;

public class PackageFileList implements Iterable<FileInfo>, Comparable<PackageFileList> {
	/**
	 * other classes rely on the sorted iteration this provides
	 */
	private TreeSet<FileInfo> files = new TreeSet<FileInfo>();

	private String id;

	public PackageFileList(String id) {
		this.id = id;
	}

	public boolean isEmpty() {
		return files.isEmpty();
	}

	public void add(FileInfo file) {
		file.clean();
		files.add(file);
	}

	public void remove(FileInfo file) {
		file.clean();
		files.remove(file);
	}

	public int size() {
		return files.size();
	}

	/**
	 * iterate the files in ascending order by their filename
	 */
	public Iterator<FileInfo> iterator() {
		return files.iterator();
	}

	public Iterator<FileInfo> descendingIterator() {
		return files.descendingIterator();
	}
	
	public String getId() {
		return id;
	}

	public int compareTo(PackageFileList o) {
		return -(o.id.compareTo(id));
	}

	@Override public String toString() {
		return id + "[" + files.toString() + "]";
	}
}