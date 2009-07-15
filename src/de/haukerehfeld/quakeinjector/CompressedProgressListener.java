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

/**
 * Report progress from the uncompressed values where only the compressed size is known
 */
public class CompressedProgressListener implements ProgressListener {
	private final double compressionRatio;
	private final ProgressListener progress;

	public CompressedProgressListener(double compressionRatio,
	                                  ProgressListener progress) {
		this.compressionRatio = compressionRatio;
		this.progress = progress;
	}

	/**
	 * tell the reporter of the size of uncompression
	 */
	public void publish(long writtenBytes) {
		long downloaded = (long) (writtenBytes * compressionRatio);
		if (downloaded <= 0) {
			System.err.println("Reporting less than zero! " + downloaded);
		}
		progress.publish(downloaded);
	}
}
	 
