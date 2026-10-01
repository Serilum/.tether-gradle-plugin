package com.serilum.tether.data;

public class Library {
	public String modId;
	public String mavenGroup;
	public String versionProperty;

	public Library(String modId, String mavenGroup, String versionProperty) {
		this.modId = modId;
		this.mavenGroup = mavenGroup;
		this.versionProperty = versionProperty;
	}
}
