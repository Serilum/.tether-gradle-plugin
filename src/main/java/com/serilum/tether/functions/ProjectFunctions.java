package com.serilum.tether.functions;

import org.gradle.api.Project;

public class ProjectFunctions {
	public static String getLoader(Project project) {
		String name = project.getName();
		if (name.equals("Common") || name.equals("Fabric") || name.equals("Forge") || name.equals("NeoForge")) {
			return name.toLowerCase();
		}

		return "";
	}

	public static String getProperty(Project project, String key) {
		Object value = project.findProperty(key);
		if (value == null) {
			return "";
		}

		return value.toString();
	}

	public static String getMinecraftVersion(Project project) {
		String displayVersion = getProperty(project, "minecraft_display_version");
		if (!displayVersion.equals("")) {
			return displayVersion;
		}

		return getProperty(project, "minecraft_version");
	}

	public static boolean isObfuscated(String minecraftVersion) {
		return minecraftVersion.startsWith("1.");
	}

	public static boolean forgeUsesDeobf(String minecraftVersion) {
		if (!isObfuscated(minecraftVersion)) {
			return false;
		}

		String[] versionParts = minecraftVersion.split("\\.");
		return Integer.parseInt(versionParts[1]) < 21;
	}
}
