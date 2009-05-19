package de.haukerehfeld.quakeinjector;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import javax.swing.event.ChangeListener;

public class Package {

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
	private List<Package> requirements;
	private List<String> unavailableRequirements;

	public Package(String id,
				   String author,
				   String title,
				   int size,
				   Date date,
				   boolean isInstalled) {
		this(id, author, title, size, date, isInstalled, null, null, null, null, null);
	}

	public Package(String id,
				   String author,
				   String title,
				   int size,
				   Date date,
				   boolean isInstalled,
				   String relativeBaseDir,
				   String commandline,
				   List<String> startmaps,
				   List<Package> requirements,
				   List<String> unavailableRequirements) {
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
		this.unavailableRequirements = unavailableRequirements;
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

	public void setRequirements(List<Package> requirements) {
		this.requirements = requirements;
	}

	public List<Package> getRequirements() {
		return this.requirements;
	}

	public void setUnavailableRequirements(List<String> unavailableRequirements) {
		this.unavailableRequirements = unavailableRequirements;
	}

	public List<String> getUnavailableRequirements() {
		return this.unavailableRequirements;
	}

	public List<String> getUnmetRequirements() {
		List<String> unmet = new ArrayList<String>(this.unavailableRequirements);
		for (Package requirement: requirements) {
			if (!requirement.isInstalled()) {
				unmet.add(requirement.getId());
			}
		}
		
		return unmet;
	}

	@Override
	public String toString() {
		return id;
	}
}

