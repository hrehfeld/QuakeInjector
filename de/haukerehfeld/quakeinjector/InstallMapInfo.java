package de.haukerehfeld.quakeinjector;

import java.net.*;
import java.io.*;
import java.util.zip.*;

import java.util.ArrayList;

import de.haukerehfeld.quakeinjector.gui.ProgressListener;
import java.awt.*;
import javax.swing.*;
import java.lang.RuntimeException;

/**
 * Install maps in a worker thread
 * Init once and let swing start it - don't reuse
 */
public class InstallMapInfo extends SwingWorker<MapFileList, Void> {
	private final int READSIZE = 40960;

	private String url;
	private String baseDirectory;
	private MapInfo map;

	public InstallMapInfo(MapInfo map,
					 String url,
					 String baseDirectory) {
		this.map = map;
		this.url = url;
		this.baseDirectory = baseDirectory;
	}

	@Override
	public MapFileList doInBackground() {
		try {
			System.out.println("Installing " + map.getId());

			MapFileList files = download(url);

			return files;
		}
		catch (java.io.IOException e) {
			/** 2009-03-29 17:38 hrehfeld    display error message */
			System.out.println("failed to download map " + e.getMessage());
		}
		return null;
	}

	@Override
    public void done() {
		map.setInstalled(true);
		
	}

	public MapFileList download(String urlString) throws java.io.IOException {
		URL url;
		try {
			url = new URL(urlString);
		}
		catch (java.net.MalformedURLException e) {
			throw new RuntimeException("Something is wrong with the way we construct URLs");
		}

		URLConnection con = url.openConnection();
		InputStream in = (InputStream) url.getContent();

		ProgressMonitorInputStream progress = new ProgressMonitorInputStream(null,
																			 "Downloading " + map.getId(),
																			 in);
		progress.getProgressMonitor().setMaximum(con.getContentLength());


		String relativedir = map.getRelativeBaseDir();
		String unzipdir = baseDirectory;
		if (relativedir != null) {
			unzipdir += File.separator + relativedir;
		}
		
		return unzip(progress, this.baseDirectory, unzipdir, map.getId());
	}

	public MapFileList unzip(InputStream in,
							 String basedir,
							 String unzipdir,
							 String mapid) {
		MapFileList files = new MapFileList(mapid);
		try {
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(in));
			ZipEntry entry;
			while((entry = zis.getNextEntry()) != null) {
				File f = new File(unzipdir + File.separator + entry.getName());
				//System.out.println("Processing " + f);

				ArrayList<File> createdDirs = mkdirs(f);
				for (File dirname: createdDirs) {
					String relative = RelativePath.getRelativePath(new File(basedir), dirname);
					//System.out.println("adding to installpaths: " + relative);
					files.add(relative);
				}

				if (entry.isDirectory()) {
					continue;
				}

				files.add(RelativePath.getRelativePath(new File(basedir), f));
//				System.out.println("adding to installpaths: " + RelativePath.getRelativePath(new File(basedir), f));
				System.out.println("Writing " + f);
				writeFile(zis, f);
			}
			zis.close();
		} catch(Exception e) {
			e.printStackTrace();
		}

		return files;
	}

	private ArrayList<File> mkdirs(File f) {
		ArrayList<File> files = new ArrayList<File>();
		
		if (f.isDirectory()) {
			files.add(f);
		}

		File parentDir = f.getParentFile();
		while (!parentDir.exists()) {
			files.add(parentDir);
			parentDir = parentDir.getParentFile();
		}

		java.util.Collections.reverse(files);

		for (File dir: files) {
			System.out.println("Creating dir " + dir);
			dir.mkdir();
		}

		return files;
	}

	private static void writeFile(InputStream in, File file) throws FileNotFoundException, IOException {
		writeFile(in, file, 2048);
	}
		
	private static void writeFile(InputStream in, File file, int BUFFERSIZE)
		throws FileNotFoundException, IOException {
		byte data[] = new byte[BUFFERSIZE];
		BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(file),
															 BUFFERSIZE);
		int readcount;
		while ((readcount = in.read(data, 0, BUFFERSIZE)) != -1) {
			dest.write(data, 0, readcount);
		}
		dest.flush();
		dest.close();
	}
	

}