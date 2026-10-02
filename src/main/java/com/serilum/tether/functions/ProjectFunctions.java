package com.serilum.tether.functions;

import org.gradle.api.Project;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectFunctions {
	private static final int[] LAST_NATAMUS_COLLECTIVE_VERSION = { 26, 3, 0 };
	private static final Pattern VERSION_NUMBER = Pattern.compile("\\d+(\\.\\d+)*");

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

	public static boolean isAfterNatamusCollective(String minecraftVersion) {
		int[] versionParts = getVersionParts(minecraftVersion);
		for (int i = 0; i < versionParts.length; i++) {
			if (versionParts[i] != LAST_NATAMUS_COLLECTIVE_VERSION[i]) {
				return versionParts[i] > LAST_NATAMUS_COLLECTIVE_VERSION[i];
			}
		}

		return false;
	}

	private static int[] getVersionParts(String minecraftVersion) {
		int[] versionParts = new int[3];

		Matcher matcher = VERSION_NUMBER.matcher(minecraftVersion);
		if (!matcher.find()) {
			return versionParts;
		}

		String[] numbers = matcher.group().split("\\.");
		for (int i = 0; i < Math.min(numbers.length, versionParts.length); i++) {
			versionParts[i] = Integer.parseInt(numbers[i]);
		}

		return versionParts;
	}
}
