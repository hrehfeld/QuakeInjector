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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

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
			System.out.println("Setting requirements");
			requirementList.setRequirements(requirement);

			for (PackageFileList l: installed) {
				requirementList.setInstalled(l);
			}

			requirementList.notifyChangeListeners();
			
		}
	}
}
