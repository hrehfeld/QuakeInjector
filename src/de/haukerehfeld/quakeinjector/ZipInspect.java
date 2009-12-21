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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.haukerehfeld.quakeinjector.gui.ProgressPopup;
import de.haukerehfeld.quakeinjector.packagelist.model.PackageListModel;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


import java.beans.XMLEncoder;

public class ZipInspect {
	private final static String file = "zipFiles.xml";

	public static void main(String[] args) {
		Configuration config = new Configuration();
		final PackageDatabaseParserWorker requirementsParser = new PackageDatabaseParserWorker(config.RepositoryDatabasePath.get());
		requirementsParser.execute();

		File parentDir = new File(args[0]);

		List<File> files = new ArrayList<File>();
		files.add(parentDir);

		int i = 0;
		while (i < files.size()) {
			File dir = files.get(i);
			
			if (dir.isDirectory()) {
				File[] zips = dir.listFiles(new java.io.FileFilter() {
						@Override
						public boolean accept(File file) {
							return file.isDirectory() || file.getName().endsWith(".zip");
						}
					});

				for (File f: zips) {
					files.add(f);
				}
			}
			i++;

		}

		Map<String, Iterable<FileInfo>> packageFiles = new HashMap<String,Iterable<FileInfo>>();

		int j = 0;
		for (File f: files) {
			if (j++ > 10) {
				//break;
			}
			if (f.isDirectory()) {
				continue;
			}
			
			System.out.println(f);

			try {
				FileInputStream in = new FileInputStream(f);
				InspectZipWorker inspector = new InspectZipWorker(in);
				inspector.execute();

				final List<ZipEntry> entries = inspector.get();

				final List<FileInfo> zipFiles = new ArrayList<FileInfo>(entries.size());

				for (ZipEntry e: entries) {
					zipFiles.add(new FileInfo(e.getName(), e.getCrc()));
				}


				String packageName = f.getName();

				//hack of ".zip"
				int ext = packageName.lastIndexOf(".");
				packageName = packageName.substring(0, ext);
				
				
				packageFiles.put(packageName, zipFiles);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		{
			boolean checkDuplicates = true;
			List<Requirement> requirements;
			try {
				requirements = requirementsParser.get();
			}
			catch (Exception e) {
				System.err.println("COuldn't get packages " + e);
				requirements = null;
				checkDuplicates = false;
			}

			if (checkDuplicates) {
				Map<String, String> dirs = new HashMap<String, String>(requirements.size());
				for (Requirement r: requirements) {
					if (r instanceof Package) {
						dirs.put(r.getId(), ((Package) r).getRelativeBaseDir());
					}
				}
				requirements = null;

				Map<String, List<String>> duplicateFiles = new HashMap<String, List<String>>();
				for (String id: packageFiles.keySet()) {
					for (FileInfo info: packageFiles.get(id)) {
						String dir = dirs.get(id);
						String file = "";
						if (dir != null) {
							file = dir;
						}
						file += info.getName();
						List<String> dupMaps = duplicateFiles.get(file);
						if (dupMaps == null) {
							dupMaps = new ArrayList<String>();
							duplicateFiles.put(file, dupMaps);
						}
						dupMaps.add(id);
					}
				}
				

				for (String file: duplicateFiles.keySet()) {
					List<String> dups = duplicateFiles.get(file);
					int count = dups.size();
					if (count > 1) {
						System.out.println(file + " has " + count + " duplicates: " + Utils.join(dups, ", "));
					}
				}
			}
		}

		

		try {
			new InstalledPackageList(new File(file)).write(packageFiles);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}