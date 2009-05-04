package de.haukerehfeld.quakeinjector;

public class Paths {
	private final static String onlineRepositoryExtension = ".zip";

	private String onlineRepositoryBase;
	private String quakeBase;

	public Paths(String onlineRepositoryBase,
				 String quakeBase) {
		this.onlineRepositoryBase = onlineRepositoryBase;
		this.quakeBase = quakeBase;
	}

	public String getRepositoryUrl(String mapid) {
		return onlineRepositoryBase + mapid + onlineRepositoryExtension;
	}

	public String getQuakeBase() {
		return quakeBase;
	}
}