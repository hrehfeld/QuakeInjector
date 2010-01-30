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

import java.io.File;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

import java.util.Collections;

import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;


public class SaveInstalled {
	private final File file;

	public SaveInstalled(File file) {
		this.file = file;
	}

	private OutputStream getOutputStream() throws java.io.IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
		return new BufferedOutputStream(new FileOutputStream(file));
	}

	private InputStream getInputStream() throws java.io.IOException {
		return new BufferedInputStream(new FileInputStream(file));
	}
	

	public void write(Iterable<? extends Requirement> list) throws java.io.IOException {
		OutputStream out = getOutputStream();
		new InstalledPackageList().write(out, list);
		out.close();
	}
	
	public void write(Map<String,Iterable<FileInfo>> files) throws java.io.IOException {
		OutputStream out = getOutputStream();
		new InstalledPackageList().write(out, files);
		out.close();
	}

	public List<PackageFileList> read() throws java.io.IOException {
		InputStream in = getInputStream();
		List<PackageFileList> result = new InstalledPackageList().read(in);
		in.close();
		return result;
	}

}
	
