package com.serilum.tether.functions;

import org.gradle.api.GradleException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

public class ManifestFunctions {
	public static HashMap<String, String> getRequiredDependencies(File modsToml) {
		HashMap<String, String> requiredDependencies = new HashMap<>();

		List<String> lines;
		try {
			lines = Files.readAllLines(modsToml.toPath());
		}
		catch (IOException ex) {
			throw new GradleException("[Tether] Unable to read " + modsToml.getPath() + ".", ex);
		}

		boolean inDependency = false;
		String modId = "";
		String versionRange = "";
		boolean required = false;

		for (String rawLine : lines) {
			String line = rawLine.strip();

			if (line.startsWith("[")) {
				if (inDependency && required) {
					requiredDependencies.put(modId, versionRange);
				}

				inDependency = line.startsWith("[[dependencies.");
				modId = "";
				versionRange = "";
				required = false;
				continue;
			}

			if (!inDependency || !line.contains("=")) {
				continue;
			}

			String key = line.substring(0, line.indexOf("=")).strip();
			String value = line.substring(line.indexOf("=") + 1).strip().replace("\"", "");

			switch (key) {
				case "modId" -> modId = value;
				case "versionRange" -> versionRange = value;
				case "mandatory" -> required = required || value.equals("true");
				case "type" -> required = required || value.equals("required");
			}
		}

		if (inDependency && required) {
			requiredDependencies.put(modId, versionRange);
		}

		return requiredDependencies;
	}

	public static String getMinimumVersion(String versionRange) {
		String version = versionRange.replace("[", "").replace("(", "").replace("]", "").replace(")", "");
		if (version.contains(",")) {
			version = version.substring(0, version.indexOf(","));
		}

		return version.strip();
	}
}
