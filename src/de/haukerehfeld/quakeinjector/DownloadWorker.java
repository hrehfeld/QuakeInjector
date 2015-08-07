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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.SwingWorker;

/**
 * Install maps in a worker thread
 * Init once and let swing start it - don't reuse
 */
public class DownloadWorker extends SwingWorker<Long, Void> implements ProgressListener, Cancelable {
	private final static int BUFFERSIZE = 1024;
	
	private final Download download;
	private final OutputStream out;

	public DownloadWorker(final Download download,
	                      final OutputStream out) {
		this.download = download;
		this.out = out;
	}

	@Override
	public Long doInBackground() throws
	    IOException,
	    FileNotFoundException,
	    Installer.CancelledException {
		
		System.out.println("Downloading " + download);

		
		download.connect();

		//build progress filter chain
		final ProgressListener progress =
		    new SumProgressListener(
				new PercentageProgressListener(download.getSize(),
				                               new CheckCanceledProgressListener(this,
				                                                                 this)));
		

		final InputStream in = download.getStream();
		final byte data[] = new byte[BUFFERSIZE];
		long downloadSize = 0;
		int readcount;
		while ((readcount = in.read(data, 0, BUFFERSIZE)) != -1) {
			progress.publish(readcount);
			out.write(data, 0, readcount);
			downloadSize += readcount;
		}			
		out.flush();
		return downloadSize;
	}

	public void publish(long progress) {
		if (progress <= 100) {
			setProgress((int) progress);
		}
	}

	public void checkCancelled() throws Installer.CancelledException {
		if (isCancelled()) {
			System.out.println("canceling...");
			throw new Installer.CancelledException();
		}
	}
}