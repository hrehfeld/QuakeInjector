package de.haukerehfeld.quakeinjector.gui;

public class SimpleErrorEvent implements ErrorEvent {
	private Object source;

	public SimpleErrorEvent(Object source) {
		this.source = source;
	}

	public Object getSource() {
		return source;
	}
}