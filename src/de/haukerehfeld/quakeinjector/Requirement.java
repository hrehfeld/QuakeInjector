package de.haukerehfeld.quakeinjector;

import javax.swing.event.ChangeListener;

/**
 * Base class for everything that might ever be referenced as a package.
 */
public interface Requirement extends java.lang.Comparable<Requirement> {
	public void addChangeListener(ChangeListener l);
	public void removeChangeListener(ChangeListener l);
	
	/**
	 * The name of the Requirement
	 */
	public String getId();
	public boolean isInstalled();
	public void setInstalled(boolean installed);

	/**
	 * A list of files this requirement installed
	 */
	public PackageFileList getFileList();
}

