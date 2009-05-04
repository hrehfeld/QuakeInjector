package de.haukerehfeld.quakeinjector;

import java.net.*;
import java.io.*;
import java.util.zip.*;

import de.haukerehfeld.quakeinjector.gui.ProgressListener;
import java.awt.*;
import javax.swing.*;
import java.lang.RuntimeException;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * Install maps in a worker thread
 * Init once and let swing start it - don't reuse
 */
public class Uninstaller extends SwingWorker<Void, Void> {
	private String baseDirectory;
	private MapFileList files;
	private MapInfo map;

	public Uninstaller(MapInfo map, MapFileList files, String baseDirectory) {
		this.map = map;
		this.files = files;
		this.baseDirectory = baseDirectory;
	}

	@Override
	public Void doInBackground() {
		System.out.println("Uninstalling " + files.getId());
		uninstall(files);
		return null;
	}

	public void uninstall(MapFileList files) {
		ArrayList<File> deleteLater = new ArrayList<File>();

		//we rely on the descending order of paths in the file list here!
		//otherwise, dirs wouldn't get deleted last
		Iterator<String> it = files.descendingIterator();
		while (it.hasNext()) {
			String file = it.next();


			File f = new File(baseDirectory + File.separator + file);

			if (!f.delete()) {
					System.out.println("Couldn't delete " + f);
			}
			else {
				System.out.println("Deleted file " + f);
			}
		}

// 		//try deleting empty dirs now
// 		boolean lastIterationDeleted = true;
// 		while (lastIterationDeleted) {
// 			lastIterationDeleted = false;
// 			for (File f: deleteLater) {
// 				if (f.delete()) {
// 					System.out.println("Deleted directory " + f);
// 					lastIterationDeleted = true;
// 					deleteLater.remove(f);
// 				}
// 				else {
// 					if (f.list().length > 0) {
// 						System.out.println("Can't delete non-empty directory " + f);
// 					}
// 					else {
// 						System.out.println("Error: Can't delete empty directory " + f);
// 					}
// 				}
// 			}
// 		}
	}

	@Override
    public void done() {
		map.setInstalled(false);
		
	}
}