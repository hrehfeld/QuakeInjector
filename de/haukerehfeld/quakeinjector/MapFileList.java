package de.haukerehfeld.quakeinjector;

import java.util.TreeSet;

import java.lang.Iterable;
import java.util.Iterator;

public class MapFileList implements Iterable<String> {
	/**
	 * other classes rely on the sorted iteration this provides
	 */
	private TreeSet<String> files = new TreeSet<String>();

	private String id;

	public MapFileList(String id) {
		this.id = id;
	}

	public void add(String file) {
		files.add(file);
	}

	/**
	 * iterate the files in ascending order by their filename
	 */
	public Iterator<String> iterator() {
		return files.iterator();
	}

	public Iterator<String> descendingIterator() {
		return files.descendingIterator();
	}
	
	public String getId() {
		return id;
	}
}