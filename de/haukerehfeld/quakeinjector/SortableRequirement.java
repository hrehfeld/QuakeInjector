package de.haukerehfeld.quakeinjector;

public abstract class SortableRequirement implements Requirement {
	public int compareTo(Object o) {
		if (!(o instanceof Requirement)) {
			throw new java.lang.ClassCastException("Can't compare " + SortableRequirement.class
			                                       + " with " + o.getClass());
		}
		return getId().compareTo(((Requirement) o).getId());
	}

}
	