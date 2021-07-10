package de.haukerehfeld.quakeinjector;

/**
 * Requirements that can be sorted alphabetically by id
 */
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

	public int compareTo(Requirement o) {
		return getId().compareTo(((Requirement) o).getId());
	}

	@Override
	public String toString() {
		return getId();
	}

}
	