package de.haukerehfeld.quakeinjector;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
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
		Iterator<FileInfo> it = files.descendingIterator();
		while (it.hasNext()) {
			FileInfo file = it.next();


			File f = new File(baseDirectory + File.separator + file.getName());

			long supposedCrc = file.getChecksum();
			String skipMsg = "Couldn't check CRC for " + f + ", deleting...";
			if (supposedCrc != 0) {
				try {
					BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
					long crc = Utils.getCrc32(in, null);
					in.close();
					if (crc != supposedCrc) {
						System.err.println("CRC for " + f + " didn't match, not deleting");
						continue;
					}
				}
				catch (java.io.IOException e) {
					System.err.println(skipMsg);
				}
			}
			else {
				System.err.println(skipMsg);
			}

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