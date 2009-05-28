/*
Copyright 2009 Hauke Rehfeld


This file is part of QuakeInjector.

QuakeInjector is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

QuakeInjector is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with QuakeInjector.  If not, see <http://www.gnu.org/licenses/>.
*/
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

	public Iterator<Requirement> iterator() {
		return values().iterator();
	}
	
}