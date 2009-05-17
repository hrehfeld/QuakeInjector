package de.haukerehfeld.quakeinjector;

import java.io.FileNotFoundException;

public class FileNotWritableException extends FileNotFoundException {
	public FileNotWritableException() {
		super();
	}
	public FileNotWritableException(String message) {
		super(message);
	}
}