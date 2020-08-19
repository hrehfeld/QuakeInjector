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

//import java.awt.*;
import java.util.List;

import javax.swing.SwingWorker;

public class UpdateRequirementListWorker extends SwingWorker<Void, Void> {
	private final RequirementList requirementList;
	private final ThreadedGetter<List<Requirement>> requirementGetter;
	private final ThreadedGetter<List<PackageFileList>> installedGetter;

	private List<Requirement> requirement;
	private List<PackageFileList> installed;
	

	public UpdateRequirementListWorker(RequirementList requirementList,
									   ThreadedGetter<List<Requirement>> requirements,
	                                   ThreadedGetter<List<PackageFileList>> installed) {
		this.requirementList = requirementList;
		this.requirementGetter = requirements;
		this.installedGetter = installed;
	}
	
	@Override
	public Void doInBackground() {
		try {
			requirement = requirementGetter.get();
			installed = installedGetter.get();
		}			
		catch (java.util.concurrent.ExecutionException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		catch (java.lang.InterruptedException e) {
			System.err.println("Couldn't wait for results!" + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}


	@Override
	public void done() {
		synchronized (requirementList) {
			requirementList.setRequirements(requirement);

			for (PackageFileList l: installed) {
				requirementList.setInstalled(l);
			}

			requirementList.notifyChangeListeners();
		}
	}
}
