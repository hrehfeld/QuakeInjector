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
		defaults.setProperty(enginePath, "");
		defaults.setProperty(engineExecutable, "");
		defaults.setProperty(engineCommandline, "");

		return defaults;
	}

	public String getEnginePath() {
		return get(enginePath);
	}

	public void setEnginePath(String enginePath) {
		set(this.enginePath, enginePath);
	}

	public String getEngineExecutable() {
		return get(engineExecutable);
	}

	public void setEngineExecutable(String engineExecutable) {
		set(this.engineExecutable, engineExecutable);
	}

	public String getEngineCommandline() {
		return get(engineCommandline);
	}

	public void setEngineCommandline(String engineCommandline) {
		set(this.engineCommandline, engineCommandline);
	}

}