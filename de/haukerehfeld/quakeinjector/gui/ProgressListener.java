package de.haukerehfeld.quakeinjector.gui;

/**
 * A class that gets called everytime something that progresses is able to
 */
public interface ProgressListener {
	/**
	 * @param progress Progress from 0 (did nothing) to 1 (completed)
	 */
	public void reportProgress(float progress);
}