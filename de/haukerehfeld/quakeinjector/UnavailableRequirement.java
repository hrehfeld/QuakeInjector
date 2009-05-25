package de.haukerehfeld.quakeinjector;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import javax.swing.event.ChangeListener;

public class UnavailableRequirement extends SortableRequirement implements Requirement {
	private final String id;
	
	public void addChangeListener(ChangeListener l) { return; }

	public UnavailableRequirement(String id) {
		this.id = id;
	}

	/**
	 * get id
	 */
	public String getId() { return id; }

	public boolean isInstalled() { return false; }

	public String toString() {
		return getId();
	}
}

