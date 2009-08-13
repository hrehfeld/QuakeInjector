package de.haukerehfeld.quakeinjector;

public abstract class SortableRequirement implements Requirement {
	private final String id;
	private boolean isInstalled = false;


	public SortableRequirement(String id) {
		this.id = id;
	}

	/**
	 * get id
	 */
	public String getId() { return id; }

	public boolean isInstalled() {
		return isInstalled;
	}

	public void setInstalled(boolean installed) {
		isInstalled = installed;
		
		notifyChangeListeners();
	}

	protected abstract void notifyChangeListeners();

	public int compareTo(Object o) {
		if (!(o instanceof Requirement)) {
			throw new java.lang.ClassCastException("Can't compare " + SortableRequirement.class
			                                       + " with " + o.getClass());
		}
		return getId().compareTo(((Requirement) o).getId());
	}

	@Override
	public String toString() {
		return getId();
	}

}
	