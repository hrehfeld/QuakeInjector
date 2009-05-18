package de.haukerehfeld.quakeinjector;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.SwingWorker;

/**
 * Install maps in a worker thread
 * Init once and let swing start it - don't reuse
 */
public class InstallWorker extends SwingWorker<PackageFileList, Void> {
	private String url;
	private String baseDirectory;
	private Package map;

	private long downloadSize = 0;
	private long downloaded = 0;

	private PackageFileList files;

	public InstallWorker(Package map,
					 String url,
					 String baseDirectory) {
		this.map = map;
		this.url = url;
		this.baseDirectory = baseDirectory;
	}

	@Override
	public PackageFileList doInBackground() throws IOException, FileNotFoundException, Installer.CancelledException {
		System.out.println("Installing " + map.getId());

		try {
			PackageFileList files = download(url);
		}
		catch (Installer.CancelledException e) {
			System.out.println("cancelled exception!");
			//throw e;
			throw new OnlineFileNotFoundException();
		}
		map.setInstalled(true);
		return files;
	}

	public PackageFileList download(String urlString) throws java.io.IOException, Installer.CancelledException {
		URL url;
		try {
			url = new URL(urlString);
		}
		catch (java.net.MalformedURLException e) {
			throw new RuntimeException("Something is wrong with the way we construct URLs");
		}

		URLConnection con;
		InputStream in;
		try {
			con = url.openConnection();
			this.downloadSize = con.getContentLength();
			in = (InputStream) url.getContent();
		}
		catch (FileNotFoundException e) {
			throw new OnlineFileNotFoundException(e.getMessage());
		}


		String relativedir = map.getRelativeBaseDir();
		String unzipdir = baseDirectory;
		if (relativedir != null) {
			unzipdir += File.separator + relativedir;
		}
		
		return unzip(in, this.baseDirectory, unzipdir, map.getId());
	}

	public PackageFileList unzip(InputStream in,
							 String basedir,
							 String unzipdir,
							 String mapid)
		throws IOException, FileNotFoundException, Installer.CancelledException {
		files = new PackageFileList(mapid);

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
			System.out.println("Writing " + f + " (" + entry.getCompressedSize() + "b)");
			try {
				writeFile(zis, f,
						  new WriteToDownloadProgress(entry.getCompressedSize(), entry.getSize()));
			}
			catch (FileNotFoundException e) {
				throw new FileNotWritableException(e.getMessage());
			}
			
		}
		//save the mapfile list so we can uninstall
		zis.close();

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

	private void writeFile(InputStream in, File file, WriteToDownloadProgress progress)
		throws FileNotFoundException, IOException, Installer.CancelledException {
		writeFile(in, file, 2048, progress);
	}
		
	private void writeFile(InputStream in, File file, int BUFFERSIZE, WriteToDownloadProgress progress)
		throws FileNotFoundException, IOException, Installer.CancelledException {
		byte data[] = new byte[BUFFERSIZE];
		BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(file),
															 BUFFERSIZE);
		int readcount;
		while ((readcount = in.read(data, 0, BUFFERSIZE)) != -1) {
			progress.publish(readcount);
			dest.write(data, 0, readcount);
		}
		dest.flush();
		dest.close();
	}

	private class WriteToDownloadProgress {
		private long downloadSize;
		private long writeSize;

		public WriteToDownloadProgress(long downloadSize, long writeSize) {
			this.downloadSize = downloadSize;
			this.writeSize = writeSize;
			if (writeSize <= 0) {
				System.out.println("writeSize " + writeSize);
			}
		}

		public void publish(int writtenBytes) throws Installer.CancelledException {
			long downloaded = downloadSize * writtenBytes / writeSize;
			addDownloaded(downloaded);
		}
	}
	
	private void addDownloaded(long read) throws Installer.CancelledException {
		//we do this here because this is the most frequently called portion
		checkCancelled();
		
		downloaded += read;
		int progress = (int) (100 * downloaded / downloadSize);
		//System.out.println("Progress(%): " + progress);
		if (progress <= 100) {
			setProgress(progress);
		}
	}

	private void checkCancelled() throws Installer.CancelledException {
		if (isCancelled()) {
			System.out.println("canceling...");
			throw new Installer.CancelledException();
		}
	}

	public PackageFileList getInstalledFiles() {
		return files;
	}
}