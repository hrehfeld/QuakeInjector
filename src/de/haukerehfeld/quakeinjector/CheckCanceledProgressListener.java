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
 * Check if canceledd on each write
 */
public class CheckCanceledProgressListener implements ProgressListener {
	private final ProgressListener progress;
	private final InstallWorker toCheck;

	public CheckCanceledProgressListener(InstallWorker toCheck, ProgressListener progress) {
		this.progress = progress;
		this.toCheck = toCheck;
	}

	/**
	 * tell the reporter of the size of uncompression
	 */
	public void publish(long writtenBytes) {
		toCheck.checkCancelled();
		progress.publish(writtenBytes);
	}
}
	 
