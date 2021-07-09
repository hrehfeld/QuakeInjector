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

import java.util.Map;
import java.util.HashMap;

import java.lang.reflect.Field;

/**
 * Handle the config/properties file and allow access to properties
 */
public class Configuration {
	private static final String CONFIGHEADER = "Quake Injector " + BuildCommit.getBuildCommit() + " config file";
	public class EnginePath extends FileValue {
		private EnginePath() { super("enginePath", null); }
		
		public File getUnzipDir(Package map) {
			String relativedir = map.getRelativeBaseDir();
			String unzipdir = get().getAbsolutePath();
			if (relativedir != null) {
				unzipdir += File.separator + relativedir;
			}
			return new File(unzipdir);
		}
	}
	public final EnginePath EnginePath = new EnginePath();

	public class EngineExecutable extends FileValue {
		private EngineExecutable() { super("engineExecutable", new File("")); }
	}
	public final EngineExecutable EngineExecutable = new EngineExecutable();

	public class WorkingDirAtExecutable extends BooleanValue {
		private WorkingDirAtExecutable() { super("workingDirAtExecutable", false); }
	}
	public final WorkingDirAtExecutable WorkingDirAtExecutable = new WorkingDirAtExecutable();

	public class LocalDatabaseFile extends FileValue {
		private LocalDatabaseFile() { super("localDatabaseFile", new File("database.xml")); }
	}
	public final LocalDatabaseFile LocalDatabaseFile = new LocalDatabaseFile();
	
	public class DownloadPath extends FileValue {
		private DownloadPath() { super("downloadPath", null); }
		public File get() {
			File result = super.get();
			if (result == null) {
				System.out.println("downloadpath null, returning default");
				return defaultPath();
			}
			return result;
		}

		private File defaultPath() {
			if (EnginePath.get() == null) {
				return null;
			}
			return new File(EnginePath.get() + File.separator + "downloads");
		}

		public void set(File v) {
			System.out.println("Setting downloadpath: " + v);
			if (v.equals(defaultPath()) || v.equals(new File(""))) {
				System.out.println(getClass() + " Setting downloadpath to null");
				super.set(null);
				return;
			}
			super.set(v);
		}
	}
	public final DownloadPath DownloadPath = new DownloadPath();

	public class EngineCommandLine extends StringValue {
		private EngineCommandLine() { super("engineCommandline", ""); }
	}
	public final EngineCommandLine EngineCommandLine = new EngineCommandLine();

	public class RepositoryDatabasePath extends StringValue {
		private RepositoryDatabasePath() { super("repositoryDatabase",
				"https://www.quaddicted.com/reviews/quaddicted_database.xml"); }
	}
	public final RepositoryDatabasePath RepositoryDatabasePath = new RepositoryDatabasePath();
	
	public class ScreenshotRepositoryPath extends StringValue {
		private ScreenshotRepositoryPath() { super("screenshotRepositoryPath",
				"https://www.quaddicted.com/reviews/screenshots/"); }
	}
	public final ScreenshotRepositoryPath ScreenshotRepositoryPath = new ScreenshotRepositoryPath();

	public class ZipContentsDatabaseUrl extends StringValue {
		private ZipContentsDatabaseUrl() { super("ZipContentsDatabaseUrl",
				"http://haukerehfeld.de/projects/quakeinjector/temp/zipContents.xml"); }
	}
	public final ZipContentsDatabaseUrl ZipContentsDatabaseUrl = new ZipContentsDatabaseUrl();
	

	public class RogueInstalled extends BooleanValue {
		private RogueInstalled() { super("rogueInstalled", false); }
	}
	public final RogueInstalled RogueInstalled = new RogueInstalled();

	public class OfflineMode extends BooleanValue {
		private ChangeListenerList listeners = new ChangeListenerList();
		
		private OfflineMode() { super("offlineMode", false); }
		
		public void addChangeListener(javax.swing.event.ChangeListener l) {
			listeners.addChangeListener(l);
		}

		@Override
		public void set(Boolean b) {
			super.set(b);
			listeners.notifyChangeListeners(this);
		}
	}
	public final OfflineMode OfflineMode = new OfflineMode();

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
		
		private RepositoryBasePath() { super("repositoryBase", "https://www.quaddicted.com/filebase/"); }

		/**
		 * Get a complete Url to a map archive file in the repo
		 */
		public String getRepositoryUrl(String mapid) {
			return get() + mapid + onlineRepositoryExtension;
		}
	}
	public final RepositoryBasePath RepositoryBasePath = new RepositoryBasePath();

	public final Map<String,Value<?>> All = new HashMap<String,Value<?>>();
	
	private File configFile;

	public Configuration(File configFile) {
		this.configFile = configFile;
		
//		//assign all fields to all list;
//		Field[] fields = getClass().getDeclaredFields();
//		for (Field f: fields) {
//			//System.out.println(f.getType());
//			//only value fields
//			if (Value.class.isAssignableFrom(f.getType())) {
//				try {
//					All.put(f.getName(), (Value) f.get(this));
//				}
//				catch (java.lang.IllegalAccessException e) {
//					e.printStackTrace();
//				}
//			}
//		}

		init();
	}

	@SuppressWarnings("unchecked")
	private void set(Configuration c) {
		for (Map.Entry<String,Value<?>> e: c.All.entrySet()) {
			String key = e.getKey();
			Value<?> v = e.getValue();

			if (!v.equals(getValue(key))) {
				getValue(key).set(v.get());
			}
		}
	}

	private Value getValue(String key) {
		return All.get(key);
	}


	/**
	 * Make sure config is loaded
	 */
	public void init() {
		read();
	}

	/**
	 * Read the properties file or get default properties
	 */
	private void read() {
		System.out.println("Reading configuration...");
		Properties properties = new Properties(defaults());
		//config exists
		if (configFile.canRead()) {
			try {
				FileInputStream in = new FileInputStream(configFile);
				properties.load(in);
				in.close();
			}
			catch (java.io.FileNotFoundException e) {
				//this should never happen cause we just checked if we
				//can read -- but maybe another process fucks up...
				System.out.println("Can't read config file (" + e.getMessage() + ") even though i just checked if i can "
								   + "read it. Using defaults...");

			}
			catch (java.io.IOException e) {
				// if we can't read the config file, just use the defaults
				System.out.println("Couldn't read config file: " + e.getMessage() + ". Using defaults...");
			}
		}

		set(properties);
	}

	@SuppressWarnings("unchecked")
	public void set(Properties p) {
		for (String key: All.keySet()) {
			String s = p.getProperty(key);
			if (s == null) {
				continue;
			}
			
			Value v = All.get(key);
			v.set(v.stringToValue(s));
			//System.out.println("Setting " + key + ": " + p.getProperty(key));
		}
	}

	public void get(Properties p) {
		for (String key: All.keySet()) {
			Value v = All.get(key);
			if (v.exists()) {
				//System.out.println("Writing " + key + " to " + v.toString() + " from " + v.getClass() + ": " + v);
				p.setProperty(key, v.toString());
			}
			else {
				if (p.getProperty(key) != null) {
					p.remove(key);
				}
			}
		}
	}
	

	public void write() throws java.io.IOException {
		if (!Utils.canWriteToDirectory(configFile.getParentFile())) {
			System.out.println("Cannot write to config directory!");
			throw new java.io.FileNotFoundException("Cannot write to config directory");
		}
		System.out.print("Writing configuration...");

		try {
			Properties properties = new Properties(defaults());
			get(properties);
			FileOutputStream out = new FileOutputStream(configFile);
			properties.store(out, CONFIGHEADER);
			out.close();
		}
		catch (java.io.FileNotFoundException e) {
			System.err.println("Can't write config file: " + e.getMessage());
			e.printStackTrace();
		}
		catch (java.io.IOException e) {
			System.err.println("Can't write config file: " + e.getMessage());
			e.printStackTrace();
		}
		System.out.println("done.");
	}

	/**
	 * Get Default properties
	 */
	private Properties defaults() {
		Properties defaults = new Properties();

		//System.out.println("Setting defaults: ");

		for (String key: All.keySet()) {
			Value<?> v = All.get(key);
			Object value = v.defaultValue();
			//System.out.println("Setting defaults: " + v.key() + ", " + v.defaultValue());
			if (value == null) {
				continue;
			}
			defaults.setProperty(v.key(), v.toString());
		}
		return defaults;
	}

	public interface Value<T> {
		public T get();
		public void set(T v);
		public String key();
		public T defaultValue();
		public T stringToValue(String s);

		/**
		 * @return true if this has a value different from the default one
		 */
		public boolean exists();
		
		/**
		 * @return true if exists() or has a defaultvalue
		 */
		public boolean existsOrDefault();
	}

	public abstract class AbstractValue<T> implements Value<T> {
		private String key;
		private T defaultValue;
		private T value;

		protected AbstractValue(String key, T defaultValue) {
			this.key = key;
			this.defaultValue = defaultValue;
		}

		public abstract T stringToValue(String v);

		public String key() {
			return key;
		}

		public T defaultValue() {
			return defaultValue;
		}

		public boolean existsOrDefault() {
			return exists() || defaultValue != null;
		}

		public T get() {
			if (value == null) {
				return defaultValue;
			}
			return value;
		}

		public void set(T v) {
			if (v == null || v.equals(defaultValue())) {
				//System.out.println(getClass() + ": Setting to null or default");
				value = null;
				return;
			}
			value = v;
		}

		public boolean exists() {
			return value != null;
		}

		public String toString() {
			return existsOrDefault() ? get().toString() : null;
		}
	}

	public abstract class StringValue extends AbstractValue<String> {
		protected StringValue(String key, String defaultValue) { super(key, defaultValue); }
		
		public String stringToValue(String v) {
			return v;
		}

		public String toString() {
			return get();
		}
	}

	public abstract class FileValue extends AbstractValue<File> {
		protected FileValue(String key, File defaultValue) { super(key, defaultValue); }
		
		public File stringToValue(String v) {
			return new File(v);
		}
	}
	

	public abstract class BooleanValue extends AbstractValue<Boolean> {
		protected BooleanValue(String key, boolean defaultValue) { super(key, defaultValue); }
		
		public Boolean stringToValue(String v) {
			return Boolean.valueOf(v);
		}
	}

	public abstract class IntegerValue extends AbstractValue<Integer> {
		protected IntegerValue(String key, Integer defaultValue) { super(key, defaultValue); }
		
		public Integer stringToValue(String v) {
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
