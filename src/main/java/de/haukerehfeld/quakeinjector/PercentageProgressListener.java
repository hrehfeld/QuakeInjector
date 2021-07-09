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
 * Report Percentages instaedd of absolute values
 */
public class PercentageProgressListener implements ProgressListener {
	private final long size;
	private final ProgressListener progress;

	public PercentageProgressListener(long size, ProgressListener progress) {
		// if (size <= 0) {
		// 	throw new IllegalArgumentException("Size must be > 0: was " + size);
		// }

		this.progress = progress;
		this.size = size;
	}

	/**
	 * tell the reporter of the size of uncompression
	 */
	public void publish(long writtenBytes) {
		long per;
		if (size == 0) {
			per = 0;
		}
		else {
			per = Math.round(100 * writtenBytes / (double) size);			
		}
		progress.publish(per);
	}
}
	 
