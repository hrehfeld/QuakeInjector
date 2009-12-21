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
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

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

import java.io.Console;


public class ZipInspect {
	private final static String file = "zipFiles.xml";

	public static void main(String[] args) {
		Console con = System.console();
		if (con == null) {
			System.err.println("Don't start from ant!");
			System.exit(1);
		}


		Configuration config = new Configuration();
		final PackageDatabaseParserWorker requirementsParser = new PackageDatabaseParserWorker(config.RepositoryDatabasePath.get());
		requirementsParser.execute();

		String parentDir = args[0];

		List<File> files = new ArrayList<File>();
		//files.add(parentDir);

		// int i = 0;
		// while (i < files.size()) {
		// 	File dir = files.get(i);
			
		// 	if (dir.isDirectory()) {
		// 		File[] zips = dir.listFiles(new java.io.FileFilter() {
		// 				@Override
		// 				public boolean accept(File file) {
		// 					return file.isDirectory() || file.getName().endsWith(".zip");
		// 				}
		// 			});

		// 		for (File f: zips) {
		// 			files.add(f);
		// 		}
		// 	}
		// 	i++;

		// }

		
		boolean checkDuplicates = true;
		List<Requirement> requirements;
		try {
			requirements = requirementsParser.get();
			java.util.Collections.sort(requirements);
		}
		catch (Exception e) {
			System.err.println("COuldn't get packages " + e);
			requirements = null;
			checkDuplicates = false;
		}


		Map<Package, Iterable<FileInfo>> packageFiles = new TreeMap<Package,Iterable<FileInfo>>();
		SortedMap<String, List<Map.Entry<Package,FileInfo>>> duplicateFiles
		    = new TreeMap<String, List<Map.Entry<Package,FileInfo>>>();
		
		int j = 0;
		for (Requirement r: requirements) {
			if (r instanceof Package) {
				Package p = (Package) r;
				p.setInstalled(true);
				
				File f = new File(parentDir + File.separator + r.getId() + ".zip");
				if (!f.exists()) {
					System.out.println("ERROR: " + f + " doesn't exist!");
					continue;
				}
				if (j++ > 10) {
					//break;
				}
				System.out.println(f);

				try {
					FileInputStream in = new FileInputStream(f);
					InspectZipWorker inspector = new InspectZipWorker(in);
					inspector.execute();

					final List<ZipEntry> entries = inspector.get();

					final PackageFileList zipFiles = new PackageFileList(p.getId());
					String dir = p.getRelativeBaseDir();

					for (ZipEntry e: entries) {
						String file = "";
						if (dir != null) {
							file = dir;
						}
						file += e.getName();

						FileInfo info = new FileInfo(file, e.getCrc());
						zipFiles.add(info);

						List<Map.Entry<Package,FileInfo>> dupMaps = duplicateFiles.get(file);
						if (dupMaps == null) {
							dupMaps = new ArrayList<Map.Entry<Package,FileInfo>>();
							duplicateFiles.put(file, dupMaps);
						}
						dupMaps.add(new AbstractMap.SimpleEntry<Package,FileInfo>(p, info));
						
					}
					p.setFileList(zipFiles);
					packageFiles.put(p, zipFiles);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		for (String file: duplicateFiles.keySet()) {
			List<Map.Entry<Package,FileInfo>> dups = duplicateFiles.get(file);
			int count = dups.size();
			if (count > 1) {
				List<String> packages = new ArrayList<String>();
				boolean crcDiffers = false;
				long crc = -1;
				for (Map.Entry<Package,FileInfo> e: dups) {
					if (crc != -1 && crc != e.getValue().getChecksum()) {
						crcDiffers = true;
					}
					crc = e.getValue().getChecksum();
					
					packages.add(e.getKey().getId() + " (crc: " + crc + ")");
				}

				boolean essential = true;
				if (!crcDiffers && crc == 0) {
					System.out.println(file + " has duplicates, but all with crc == 0, setting to inessential");
					essential = false;
				}
				else {
					System.out.println(file + " has " + (crcDiffers ? " differing CRC " : " equal ") + "duplicates in "
					                   + Utils.join(packages, ", "));

					{
						System.out.println("Is this an essential file? (n + RETURN for no, default yes)");
						String yes = con.readLine();
						if (yes != null && yes.equals("n")) {
							essential = false;
						}
					}
					// boolean incompatible = crcDiffers;
					// if (essential && crcDiffers) {
					// 	System.out.println("Are these files incompatible? (n + RETURN for no, default yes)");
					// 	String yes = con.readLine();
					// 	if (yes != null && yes.equals("n")) {
					// 		incompatible = false;
					// 	}
					// }
				}
				
				for (Map.Entry<Package,FileInfo> e: dups) {
					e.getValue().setEssential(essential);
					// e.getValue().setIncompatible(incompatible);
				}

			}
		}
		
		try {
			new InstalledPackageList(new File(file)).write(packageFiles.keySet());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}