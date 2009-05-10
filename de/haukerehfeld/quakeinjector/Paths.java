package de.haukerehfeld.quakeinjector;

public class Paths {
	private final static String onlineRepositoryExtension = ".zip";

	private String onlineRepositoryBase;

	public Paths(String onlineRepositoryBase) {
		this.onlineRepositoryBase = onlineRepositoryBase;
	}

	public String getRepositoryUrl(String mapid) {
		return onlineRepositoryBase + mapid + onlineRepositoryExtension;
	}

	public void setRepositoryBase(String url) {
		this.onlineRepositoryBase = url;
	}
}