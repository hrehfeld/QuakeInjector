package de.haukerehfeld.quakeinjector;

import java.util.HashMap;
import java.util.List;

import java.util.Comparator;
import java.lang.Iterable;
import java.util.Iterator;

public class PackageList extends HashMap<String, Requirement> implements Iterable<Requirement> {
	private final InstalledPackageList installedList = new InstalledPackageList();

	public PackageList() {}
	
	public PackageList(List<Requirement> requirements) {
		setRequirements(requirements);
	}

	public void setRequirements(List<Requirement> requirements) {
		for (Requirement r: requirements) {
			put(r.getId(), r);
		}
	}

	public void setInstalled(List<PackageFileList> files) throws java.io.IOException {
		for (PackageFileList l: files) {
			Requirement r = get(l.getId());
			if (r == null) {
				System.out.println("Warning: InstalledMaps xml contains map that isn't known: "
				                   + l.getId());
				continue;
			}
			r.setInstalled(true);
			if (r instanceof Package) { ((Package) r).setFileList(l); }
		}
		
	}

	public void writeInstalled() throws java.io.IOException {
		installedList.write(this);
	}

	public Requirement getRequirement(String id) {
		Requirement g = get(id);
		if (g == null) {
			return new UnavailableRequirement(id);
		}
		return g;
	}		


	/**
	 * Sort/compare requirements alphabetically by id
	 */
	private class RequirementIdComparator implements Comparator<Requirement> {
		public int compare(Requirement lhs, Requirement rhs) {
			return lhs.getId().compareTo(rhs.getId());
		}

		public boolean equals(Object o) {
			return (this == o);
		}
	}

	public Iterator<Requirement> iterator() {
		return values().iterator();
	}
	
}