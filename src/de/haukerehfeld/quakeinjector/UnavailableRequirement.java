package de.haukerehfeld.quakeinjector;

import javax.swing.event.ChangeListener;

public class UnavailableRequirement extends SortableRequirement implements Requirement {
	public UnavailableRequirement(String id) {
		super(id);
	}
	public void addChangeListener(ChangeListener l) {}
	protected void notifyChangeListeners() {}

	public PackageFileList getFileList() {
		return new PackageFileList(getId());
	}
}

