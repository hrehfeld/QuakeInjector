package de.haukerehfeld.quakeinjector;

import java.io.FileNotFoundException;

public class OnlineFileNotFoundException extends FileNotFoundException {
	public OnlineFileNotFoundException() {
		super();
	}
	public OnlineFileNotFoundException(String message) {
		super(message);
	}
}