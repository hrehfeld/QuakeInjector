/*
Copyright 2014 Eric Wasylishen


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
package de.haukerehfeld.quakeinjector.gui;

import java.awt.Desktop;
import java.net.URI;

public class BrowserLauncher {
	public static void openURL(String url) {
		try {
			Desktop.getDesktop().browse(URI.create(url));
		} catch (Exception e) {
			System.err.println("Error browsing URL: " + url);
			e.printStackTrace(System.err);
		}		
	}
}
