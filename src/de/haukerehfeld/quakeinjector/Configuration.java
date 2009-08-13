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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Handle the config/properties file and allow access to properties
 */
public class Configuration {
	private static final String enginePath = "enginePath";
	private static final String engineExecutable = "engineExecutable";
	private static final String engineCommandline = "engineCommandline";
	private static final String repositoryDatabase = "repositoryDatabase";
	private static final String rogueInstalled = "rogueInstalled";
	private static final String hipnoticInstalled = "hipnoticInstalled";

	private static final String mainWindowPositionX = "mainWindowPositionX";
	private static final String mainWindowPositionY = "mainWindowPositionY";
	private static final String mainWindowWidth = "mainWindowWidth";
	private static final String mainWindowHeight = "mainWindowHeight";

	
	private Properties properties;
	private File configFile = new File("config.properties");

	

	/**
	 * Get a config property
	 */
	public String get(String name) {
		init();
		return properties.getProperty(name);
	}

	/**
	 * set a config property
	 */
	public void set(String name, String value) {
		init();
		properties.setProperty(name, value);
	}

	/**
	 * Make sure config is loaded
	 */
	public void init() {
		if (properties == null) {
			read();
		}
	}

	/**
	 * Read the properties file or get default properties
	 */
	private void read() {
		properties = new Properties(defaults());
		//config exists
		if (configFile.canRead()) {
			try {
				properties.load(new FileInputStream(configFile));
			}
			catch (java.io.FileNotFoundException e) {
				//this should never happen cause we just checked if we
				//can read -- but maybe another process fucks up...
				System.out.println("Can't read config file even though i just checked if i can "
								   + "read it. Using defaults...");
			}
			catch (java.io.IOException e) {
				// if we can't read the config file, just use the defaults
				System.out.println("Couldn't read config file. Using defaults...");
			}
		}
	}

	public void write() {
			try {
				properties.store(new FileOutputStream(configFile),
					"Header");
			}
			catch (java.io.FileNotFoundException e) {
				//this should never happen cause we just checked if we
				//can read -- but maybe another process fucks up...
				System.out.println("Can't read config file even though i just checked if i can "
								   + "read it. Using defaults...");
				properties = defaults();
			}
			catch (java.io.IOException e) {
				// if we can't read the config file, just use the defaults
				System.out.println("Couldn't read config file. Using defaults...");
				properties = defaults();
			}
		
	}

	/**
	 * Get Default properties
	 */
	private Properties defaults() {
		Properties defaults = new Properties();

		defaults.setProperty("downloadDirectory", "/home/hrehfeld/download/");
		defaults.setProperty("unzipDirectory", "/home/hrehfeld/games/quake/unzip/");
		
		defaults.setProperty("repositoryBase", "http://www.quaddicted.com/filebase/");
		defaults.setProperty(repositoryDatabase,
							 "http://www.quaddicted.com/reviews/quaddicted_database.xml");
		defaults.setProperty(enginePath, "");
		defaults.setProperty(engineExecutable, "");
		defaults.setProperty(engineCommandline, "");

		return defaults;
	}

	public String getEnginePath() {
		return get(enginePath);
	}

	public void setEnginePath(String enginePath) {
		set(Configuration.enginePath, enginePath);
	}

	public String getEngineExecutable() {
		return get(engineExecutable);
	}

	public void setEngineExecutable(String engineExecutable) {
		set(Configuration.engineExecutable, engineExecutable);
	}

	public String getEngineCommandline() {
		return get(engineCommandline);
	}

	public void setEngineCommandline(String engineCommandline) {
		set(Configuration.engineCommandline, engineCommandline);
	}

	public String getRepositoryDatabase() {
		return get(repositoryDatabase);
	}

	/**
	 * get rogueInstalled
	 */
	public boolean getRogueInstalled() { return Boolean.parseBoolean(get(rogueInstalled)); }

	/**
	 * set rogueInstalled
	 */
	public void setRogueInstalled(boolean rogueInstalled) { set(Configuration.rogueInstalled,
																Boolean.toString(rogueInstalled)); }

	/**
	 * get hipnoticInstalled
	 */
	public boolean getHipnoticInstalled() { return Boolean.parseBoolean(get(hipnoticInstalled)); }

	/**
	 * set hipnoticInstalled
	 */
	public void setHipnoticInstalled(boolean hipnoticInstalled) { set(Configuration.hipnoticInstalled,
																	  Boolean.toString(hipnoticInstalled)); }

	/**
	 * get mainWindowPositionX
	 */
	public int getMainWindowPositionX() {
		return Integer.parseInt(get(mainWindowPositionX));
	}
    
    /**
     * set mainWindowPositionX
     */
	public void setMainWindowPositionX(int mainWindowPositionX) {
		set(Configuration.mainWindowPositionX, Integer.toString(mainWindowPositionX));
	}

	/**
	 * get mainWindowPositionY
	 */
	public int getMainWindowPositionY() {
		return Integer.parseInt(get(mainWindowPositionY));
	}
    
    /**
     * set mainWindowPositionY
     */
	public void setMainWindowPositionY(int mainWindowPositionY) {
		set(Configuration.mainWindowPositionY, Integer.toString(mainWindowPositionY));
	}
	

	/**
	 * get mainWindowWidth
	 */
	public int getMainWindowWidth() {
		return Integer.parseInt(get(mainWindowWidth));
	}
    
    /**
     * set mainWindowWidth
     */
	public void setMainWindowWidth(int mainWindowWidth) {
		set(Configuration.mainWindowWidth, Integer.toString(mainWindowWidth));
	}
	
	/**
	 * get mainWindowHeight
	 */
	public int getMainWindowHeight() {
		return Integer.parseInt(get(mainWindowHeight));
	}
    
    /**
     * set mainWindowHeight
     */
	public void setMainWindowHeight(int mainWindowHeight) {
		set(Configuration.mainWindowHeight, Integer.toString(mainWindowHeight));
	}

	public boolean hasMainWindowSettings() {
		return get(mainWindowHeight) != null;
	}
}