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

public class Paths {
	private final static String onlineRepositoryExtension = ".zip";

	private String onlineRepositoryBase;

	public Paths(String onlineRepositoryBase) {
		this.onlineRepositoryBase = onlineRepositoryBase;
	}

	public String getRepositoryUrl(String mapid) {
		return onlineRepositoryBase + mapid + onlineRepositoryExtension;
	}

	public void setRepositoryBase(String url) {
		this.onlineRepositoryBase = url;
	}
}