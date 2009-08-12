package de.haukerehfeld.quakeinjector;

import javax.swing.event.ChangeListener;

public interface Requirement extends java.lang.Comparable {
	public void addChangeListener(ChangeListener l);
	public String getId();
	public boolean isInstalled();
	public void setInstalled(boolean installed);

	public PackageFileList getFileList();
}

