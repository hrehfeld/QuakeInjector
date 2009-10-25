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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.SwingWorker;

/**
 * Inspect a zipfile and gather all zipentries
 */
public class InspectZipWorker extends SwingWorker<List<ZipEntry>, Void> {
	private InputStream input;

	public InspectZipWorker(InputStream input) {
		this.input = input;
	}

	@Override
	public List<ZipEntry> doInBackground() throws IOException,
	    FileNotFoundException {
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(input));
		List<ZipEntry> entries = new ArrayList<ZipEntry>();

		ZipEntry entry;
		while((entry = zis.getNextEntry()) != null) {
			entries.add(entry);
			
		}
		zis.close();

		return entries;
	}
}