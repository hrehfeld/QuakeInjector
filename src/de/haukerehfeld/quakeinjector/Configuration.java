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

import java.util.List;
import java.util.ArrayList;

import java.lang.reflect.Field;

/**
 * Handle the config/properties file and allow access to properties
 */
public class Configuration {
	public class EnginePath extends StringValue {
		private EnginePath() { super("enginePath", ""); }
	}
	public final EnginePath EnginePath = new EnginePath();

	public class EngineExecutable extends StringValue {
		private EngineExecutable() { super("engineExecutable", ""); }
	}
	public final EngineExecutable EngineExecutable = new EngineExecutable();

	public class DownloadPath extends StringValue {
		private DownloadPath() { super("downloadPath", ""); }
	}
	public final DownloadPath DownloadPath = new DownloadPath();

	public class EngineCommandLine extends StringValue {
		private EngineCommandLine() { super("engineCommandline", ""); }
	}
	public final EngineCommandLine EngineCommandLine = new EngineCommandLine();

	public class RepositoryDatabasePath extends StringValue {
		private RepositoryDatabasePath() { super("repositoryDatabase",
				"http://www.quaddicted.com/reviews/quaddicted_database.xml"); }
	}
	public final RepositoryDatabasePath RepositoryDatabasePath = new RepositoryDatabasePath();

	public class RogueInstalled extends BooleanValue {
		private RogueInstalled() { super("rogueInstalled", false); }
	}
	public final RogueInstalled RogueInstalled = new RogueInstalled();

	public class HipnoticInstalled extends BooleanValue {
		private HipnoticInstalled() { super("hipnoticInstalled", false); }
	}
	public final HipnoticInstalled HipnoticInstalled = new HipnoticInstalled();

	public class MainWindowPositionX extends IntegerValue {
		private MainWindowPositionX() { super("mainWindowPositionX", null); }
	}
	public final MainWindowPositionX MainWindowPositionX = new MainWindowPositionX();

	public class MainWindowPositionY extends IntegerValue {
		private MainWindowPositionY() { super("mainWindowPositionY", null); }
	}
	public final MainWindowPositionY MainWindowPositionY = new MainWindowPositionY();

	public class MainWindowWidth extends IntegerValue {
		private MainWindowWidth() { super("mainWindowWidth", null); }
	}
	public final MainWindowWidth MainWindowWidth = new MainWindowWidth();

	public class MainWindowHeight extends IntegerValue {
		private MainWindowHeight() { super("mainWindowHeight", null); }
	}
	public final MainWindowHeight MainWindowHeight = new MainWindowHeight();

	public class RepositoryBasePath extends StringValue {
		private final static String onlineRepositoryExtension = ".zip";
		
		private RepositoryBasePath() { super("repositoryBase", "http://www.quaddicted.com/filebase/"); }

		/**
		 * Get a complete Url to a map archive file in the repo
		 */
		public String getRepositoryUrl(String mapid) {
			return get() + mapid + onlineRepositoryExtension;
		}
	}
	public final RepositoryBasePath RepositoryBasePath = new RepositoryBasePath();

	public final List<Value<?>> All = new ArrayList<Value<?>>();
	
	private Properties properties;
	private File configFile = new File("config.properties");

	public Configuration() {
		Field[] fields = getClass().getDeclaredFields();
		for (Field f: fields) {
			System.out.println(f.getType());
			if (Value.class.isAssignableFrom(f.getType())) {
				try {
					All.add((Value) f.get(this));
				}
				catch (java.lang.IllegalAccessException e) {
					System.err.println("Ooops");
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Get a config property
	 */
	private String get(String name) {
		init();
		return properties.getProperty(name);
	}

	/**
	 * set a config property
	 */
	private void set(String name, String value) {
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

		System.out.println("Setting defaults: ");

		for (Value<?> v: All) {
			Object value = v.defaultValue();
			String s = (value != null) ? value.toString() : "";
			defaults.setProperty(v.key(), s);
			System.out.println("Setting defaults: " + v.key() + ", " + v.defaultValue());
		}
		return defaults;
	}

	public interface Value<T> {
		public T get();
		public void set(T v);
		public String key();
		public T defaultValue();
	}

	private abstract class AbstractValue<T> implements Value<T> {
		private String key;
		private T defaultValue;

		protected AbstractValue(String key, T defaultValue) {
			this.key = key;
			this.defaultValue = defaultValue;
		}

		protected abstract T stringToValue(String v);

		public String key() {
			return key;
		}

		public T defaultValue() {
			return defaultValue;
		}

		public T get() {
			return stringToValue(Configuration.this.get(key));
		}

		public void set(T v) {
			Configuration.this.set(key, v.toString());
		}
	}

	private abstract class StringValue extends AbstractValue<String> {
		protected StringValue(String key, String defaultValue) { super(key, defaultValue); }
		
		protected String stringToValue(String v) {
			return v;
		}

		public String toString() {
			return get();
		}
	}

	private abstract class BooleanValue extends AbstractValue<Boolean> {
		protected BooleanValue(String key, boolean defaultValue) { super(key, defaultValue); }
		
		protected Boolean stringToValue(String v) {
			return Boolean.valueOf(v);
		}
	}

	private abstract class IntegerValue extends AbstractValue<Integer> {
		protected IntegerValue(String key, Integer defaultValue) { super(key, defaultValue); }
		
		protected Integer stringToValue(String v) {
			if (v == null) {
				return null;
			}
			try {
				return Integer.valueOf(v);
			}
			catch (java.lang.NumberFormatException e) {
				System.err.println(e);
				e.printStackTrace();

				return null;
			}
		}
	}
	

}