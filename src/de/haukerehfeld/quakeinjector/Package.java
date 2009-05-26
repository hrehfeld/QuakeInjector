package de.haukerehfeld.quakeinjector;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import javax.swing.event.ChangeListener;

public class Package extends SortableRequirement implements Requirement {

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

	private String relativeBaseDir;

	private String commandline;

	private List<String> startmaps;
	
	private List<Requirement> requirements;

	private PackageFileList fileList;

	public Package(String id,
				   String author,
				   String title,
				   int size,
				   Date date,
				   boolean isInstalled) {
		this(id, author, title, size, date, isInstalled, null, null, null, null);
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
				   List<Requirement> requirements) {
		super(id);
		this.author = author;
		this.title = title;
		this.size = size;
		this.date = date;
		super.setInstalled(isInstalled);
		this.relativeBaseDir = relativeBaseDir;
		this.commandline = commandline;
		this.startmaps = startmaps;
		this.requirements = requirements;
	}
	

	public void addChangeListener(ChangeListener l) {
		listeners.addChangeListener(l);
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

	public void setRequirements(List<Requirement> requirements) {
		this.requirements = requirements;
	}

	public List<Requirement> getRequirements() {
		return this.requirements;
	}


	public List<Package> getAvailableRequirements() {
		List<Package> avails = new ArrayList<Package>();
		for (Requirement r: requirements) {
			if (r instanceof Package) {
				avails.add((Package) r);
			}
		}
		return avails;
	}

	public List<Requirement> getUnavailableRequirements() {
		List<Requirement> unavails = new ArrayList<Requirement>();

		for (Requirement r: requirements) {
			if (!r.isInstalled() && !(r instanceof Package)) {
				unavails.add(r);
			}
		}
		return unavails;
	}

	public List<Requirement> getUnmetRequirements() {
		List<Requirement> unmet = new ArrayList<Requirement>();
		for (Requirement requirement: requirements) {
			if (!requirement.isInstalled()) {
				unmet.add(requirement);
			}
		}
		
		return unmet;
	}

	protected void notifyChangeListeners() {
		listeners.notifyChangeListeners(this);
	}

	/**
	 * get fileList
	 */
	public PackageFileList getFileList() { return fileList; }
    
/**
 * set fileList
 */
	public void setFileList(PackageFileList fileList) { this.fileList = fileList; }
}

