package de.haukerehfeld.quakeinjector;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import javax.swing.event.ChangeListener;

public class UnavailableRequirement extends SortableRequirement implements Requirement {
	public UnavailableRequirement(String id) {
		super(id);
	}
	public void addChangeListener(ChangeListener l) {}
	protected void notifyChangeListeners() {}
}

