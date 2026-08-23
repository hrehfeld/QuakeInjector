package de.haukerehfeld.quakeinjector;

import java.io.IOException;

/**
 * Thrown when a download is attempted when "offline mode" is enabled
 */
public class OfflineModeException extends IOException {
	public OfflineModeException(String url) {
		super("offline mode enabled, not fetching " + url);
	}
}
