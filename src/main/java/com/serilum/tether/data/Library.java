package com.serilum.tether.data;

import com.serilum.tether.functions.ProjectFunctions;

public class Library {
	public String modId;
	public String mavenGroup;
	public String version;
	public String minecraftVersion;

	public Library(String modId, String version, String minecraftVersion) {
		this.modId = modId;
		this.mavenGroup = getMavenGroup(modId, minecraftVersion);
		this.version = version;
		this.minecraftVersion = minecraftVersion;
	}

	private static String getMavenGroup(String modId, String minecraftVersion) {
		// Collective up to 26.3.0 was published before the move to com.serilum, so it stays in its old group.
		if (modId.equals("collective") && !ProjectFunctions.isAfterNatamusCollective(minecraftVersion)) {
			return "com.natamus.collective-ml";
		}

		return "com.serilum." + modId;
	}
}
