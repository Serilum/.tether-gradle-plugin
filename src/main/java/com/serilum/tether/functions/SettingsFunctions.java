package com.serilum.tether.functions;

import org.gradle.api.initialization.Settings;

public class SettingsFunctions {
	public static String getProperty(Settings settings, String key) {
		return settings.getProviders().gradleProperty(key).getOrElse("");
	}

	public static boolean isToggledOn(Settings settings, String toggle) {
		return !getProperty(settings, toggle).equals("false");
	}

	public static String getMinecraftVersion(Settings settings) {
		String displayVersion = getProperty(settings, "minecraft_display_version");
		if (!displayVersion.equals("")) {
			return displayVersion;
		}

		return getProperty(settings, "minecraft_version");
	}
}
