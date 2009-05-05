package de.haukerehfeld.quakeinjector;

import java.util.ArrayList;
import java.util.List;

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

	private List<String> startmaps;
	private List<MapInfo> requirements;

	public MapInfo(String id,
				   String author,
				   String title,
				   int size,
				   Date date,
				   boolean isInstalled) {
		this(id, author, title, size, date, isInstalled, null, null, null, null);
	}

	public MapInfo(String id,
				   String author,
				   String title,
				   int size,
				   Date date,
				   boolean isInstalled,
				   String relativeBaseDir,
				   String commandline,
				   List<String> startmaps,
				   List<MapInfo> requirements) {
		this.id = id;
		this.author = author;
		this.title = title;
		this.size = size;
		this.date = date;
		this.isInstalled = isInstalled;
		this.relativeBaseDir = relativeBaseDir;
		this.commandline = commandline;
		this.startmaps = startmaps;
		this.requirements = requirements;
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

	public List<String> getStartmaps() {
		return startmaps;
	}

	public boolean isInstalled() {
		return isInstalled;
	}

	public void setInstalled(boolean installed) {
		isInstalled = installed;
		
		listeners.notifyChangeListeners(this);
	}

	public void setRequirements(List<MapInfo> requirements) {
		this.requirements = requirements;
	}

	public List<MapInfo> getRequirements() {
		return this.requirements;
	}

}

