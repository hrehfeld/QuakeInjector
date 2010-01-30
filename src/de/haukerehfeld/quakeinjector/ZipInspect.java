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
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;

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

/**
 * Create an xml of all the files in the zips
 */
public class ZipInspect {
	private final static String file = "zipFiles.xml";
	private final static String oldSelectionsFile = "essentialfiles.cfg";

	public static void main(String[] args) {
		Console con = System.console();
		if (con == null) {
			System.err.println("Don't start from ant, it doesn't support an interactive console!");
			System.exit(1);
		}

		if (args.length < 1) {
			System.err.println("First parameter need's to be a valid directory!");
			System.exit(1);
		}
		String parentDir = args[0];

		if (!(new File(parentDir).exists())) {
			System.err.println("First parameter need's to be a valid directory!");
			System.exit(1);
		}

		Configuration config = new Configuration();

		List<Requirement> requirements = null;
		{
			final PackageDatabaseParserWorker requirementsParser
			    = new PackageDatabaseParserWorker(config.RepositoryDatabasePath.get());
			requirementsParser.execute();


			try {
				requirements = requirementsParser.get();
			}
			catch (Exception e) {
				System.err.println("Couldn't get packages " + e);
				e.printStackTrace();
				System.exit(1);
			}
		}
		java.util.Collections.sort(requirements);

		Map<Package, Iterable<FileInfo>> packageFiles = new TreeMap<Package,Iterable<FileInfo>>();
		SortedMap<String, List<Map.Entry<Package,FileInfo>>> duplicateFiles
		    = new TreeMap<String, List<Map.Entry<Package,FileInfo>>>();
		
		for (Requirement r: requirements) {
			//only check where package exists
			if (!(r instanceof Package)) {
				continue;
			}

			Package p = (Package) r;
			p.setInstalled(true);
			
			File f = new File(parentDir + File.separator + r.getId() + ".zip");
			if (!f.exists()) {
				System.out.println("ERROR: " + f + " doesn't exist!");
				continue;
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

					List<Map.Entry<Package,FileInfo>> dupMaps =
					    duplicateFiles.get(file.toLowerCase());
					if (dupMaps == null) {
						dupMaps = new ArrayList<Map.Entry<Package,FileInfo>>();
						duplicateFiles.put(file.toLowerCase(), dupMaps);
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

		Map<String, Boolean> oldSelections = new HashMap<String, Boolean>();
		if (new File(oldSelectionsFile).exists()) {
			//load old selections
			try {
				BufferedReader in = new BufferedReader(new FileReader(oldSelectionsFile));
				String line;
				while ((line = in.readLine()) != null) {
					String[] l = line.split(",");
					oldSelections.put(l[0], Boolean.parseBoolean(l[1]));
				}
				in.close();
			} catch (java.io.IOException e) {
				System.err.println("Couldn't read old selections file");
			}
		}
			    
		StringBuilder selectionsFile = new StringBuilder();
		
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
					System.out.println(file
					                   + " has duplicates, but all with crc == 0,"
					                   + " setting to inessential");
					essential = false;
				}
				else if (oldSelections.get(file) != null) {
					essential = oldSelections.get(file);
					System.out.println(file
					                   + " has duplicates, but was previously declared:"
					                   + " essential = " + essential);
				}
				else {
					System.out.println(file
					                   + " has "
					                   + (crcDiffers ? " differing CRC " : " equal ")
					                   + "duplicates in "
					                   + Utils.join(packages, ", "));

					System.out.println("Is this an essential file? (No: n + RETURN, Yes: RETURN)");
					String yes = con.readLine();
					if (yes != null && yes.equals("n")) {
						essential = false;
					}
				}

				for (Map.Entry<Package,FileInfo> e: dups) {
					e.getValue().setEssential(essential);
				}

				selectionsFile.append(file + "," + essential + "\n");
			}
		}

		//write old selections
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(oldSelectionsFile));
			out.write(selectionsFile.toString());
			out.close();
		}
		catch (java.io.IOException e) {
			System.err.println("Couldn't write selections to outfile! " + e);
		}

		try {
			new InstalledPackageList().write(new BufferedOutputStream(new FileOutputStream(file)),
			                                 packageFiles.keySet());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}