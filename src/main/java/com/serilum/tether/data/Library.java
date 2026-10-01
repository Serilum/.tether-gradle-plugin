package com.serilum.tether.data;

public class Library {
	public String modId;
	public String mavenGroup;
	public String version;

	public Library(String modId, String version) {
		this.modId = modId;
		this.mavenGroup = "com.natamus." + modId + "-ml";
		this.version = version;
	}
}
