package de.haukerehfeld.quakeinjector;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.SwingWorker;


/**
 * Install maps in a worker thread
 * Init once and let swing start it - don't reuse
 */
public class UninstallWorker extends SwingWorker<Void, Void> {
	private String baseDirectory;
	private PackageFileList files;

	public UninstallWorker(PackageFileList files, String baseDirectory) {
		this.files = files;
		this.baseDirectory = baseDirectory;
	}

	@Override
	public Void doInBackground() {
		uninstall(files);
		return null;
	}

	public void uninstall(PackageFileList files) {
		//we rely on the descending order of paths in the file list here!
		//otherwise, dirs wouldn't get deleted last
		final int fileCount = files.size();
		int i = 1;
		Iterator<String> it = files.descendingIterator();
		while (it.hasNext()) {
			String file = it.next();


			File f = new File(baseDirectory + File.separator + file);

			if (!f.delete()) {
				System.out.println("Couldn't delete " + f);
			}
			else {
				System.out.println("Deleted file " + f);
			}
			int progress = i * 100 / fileCount;
			setProgress(progress);
			i++;
		}
	}

	@Override
    public void done() {
	}
}