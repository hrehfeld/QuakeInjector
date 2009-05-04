package de.haukerehfeld.quakeinjector;

import java.util.ArrayList;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class MapInfo {

	/**
	 * easily have change listeners
	 */
	private ChangeListenerList listeners = new ChangeListenerList();

	/**
	 * Unique file/package identifier
	 */
	private String id;
	
	private String author;

	private String title;

	/**
	 * Size in kb?
	 */
	private int size;

	private Date date;

	private boolean isInstalled;

	private String relativeBaseDir;

	private String commandline;

	private ArrayList<String> startmaps;

	public MapInfo(String id,
				   String author,
				   String title,
				   int size,
				   Date date,
				   boolean isInstalled) {
		this(id, author, title, size, date, isInstalled, null, null, null);
	}

	public MapInfo(String id,
				   String author,
				   String title,
				   int size,
				   Date date,
				   boolean isInstalled,
				   String relativeBaseDir,
				   String commandline,
				   ArrayList<String> startmaps) {
		this.id = id;
		this.author = author;
		this.title = title;
		this.size = size;
		this.date = date;
		this.isInstalled = isInstalled;
		this.relativeBaseDir = relativeBaseDir;
		this.commandline = commandline;
		this.startmaps = startmaps;
	}
	

	public void addChangeListener(ChangeListener l) {
		listeners.addChangeListener(l);
	}

	public String getId() {
		return id;
	}

	public String getAuthor() {
		return author;
	}
	public String getTitle() {
		return title;
	}
	public int getSize() {
		return size;
	}
	public Date getDate() {
		return date;
	}

	public String getRelativeBaseDir() {
		return relativeBaseDir;
	}

	public String getCommandline() {
		return commandline;
	}

	public ArrayList<String> getStartmaps() {
		return startmaps;
	}

	public boolean isInstalled() {
		return isInstalled;
	}

	public void setInstalled(boolean installed) {
		isInstalled = installed;
		
		listeners.notifyChangeListeners(this);
	}

}

