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

package de.haukerehfeld.quakeinjector.database;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import de.haukerehfeld.quakeinjector.InstalledPackageList;
import de.haukerehfeld.quakeinjector.PackageFileList;


/**
 * Thread worker to parse the installed maps in background
 */
public class InstalledMapsParser {
	public List<PackageFileList> parse(final InputStream in) {
		List<PackageFileList> files;
		try {
			files = new InstalledPackageList().read(in);
		}
		catch (java.io.FileNotFoundException e) {
			System.out.println("Notice: installed maps file doesn't exist yet,"
			                   + " no maps installed? " + e);
			files = Collections.emptyList();
		}
		catch (java.io.IOException e) {
			System.err.println("Error: installed maps file couldn't be loaded: " + e);
			e.printStackTrace();
			files = Collections.emptyList();
		}
		return files;
	}
}

