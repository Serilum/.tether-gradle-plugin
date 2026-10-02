package com.serilum.tether.data;

import com.serilum.tether.functions.ProjectFunctions;

public class Library {
	public String modId;
	public String mavenGroup;
	public String version;

	public Library(String modId, String version, String minecraftVersion) {
		this.modId = modId;
		this.mavenGroup = getMavenGroup(modId, minecraftVersion);
		this.version = version;
	}

	private static String getMavenGroup(String modId, String minecraftVersion) {
		if (modId.equals("collective") && !ProjectFunctions.isAfterNatamusCollective(minecraftVersion)) {
			return "com.natamus.collective-ml";
		}

		return "com.serilum." + modId;
	}
}
