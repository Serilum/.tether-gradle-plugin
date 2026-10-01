package com.serilum.tether.functions;

import com.serilum.tether.data.Library;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LibraryFunctions {
	public static List<Library> getLibraries(Project project) {
		List<Library> libraries = new ArrayList<>();

		File rootDir = project.getRootDir();
		String modId = ProjectFunctions.getProperty(project, "mod_id");

		List<String> importedPackages = SourceFunctions.getImportedNatamusPackages(rootDir);
		importedPackages.remove(modId);
		if (importedPackages.isEmpty()) {
			return libraries;
		}

		File modsToml = new File(rootDir, "Common" + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "META-INF" + File.separator + "mods.toml");
		if (!modsToml.isFile()) {
			throw libraryError("The code imports " + importedPackages + ", but " + modsToml.getPath() + " does not exist.");
		}

		HashMap<String, String> requiredDependencies = ManifestFunctions.getRequiredDependencies(modsToml);
		for (String importedPackage : importedPackages) {
			if (!requiredDependencies.containsKey(importedPackage)) {
				throw libraryError("The code imports com.natamus." + importedPackage + ", but mods.toml does not list '" + importedPackage + "' as a required dependency.");
			}

			String version = ManifestFunctions.getMinimumVersion(requiredDependencies.get(importedPackage));
			if (version.equals("")) {
				throw libraryError("The '" + importedPackage + "' dependency in mods.toml has no minimum version in its versionRange.");
			}

			libraries.add(new Library(importedPackage, version));
		}

		return libraries;
	}

	private static GradleException libraryError(String message) {
		return new GradleException("[Tether] " + message + " See https://github.com/Serilum/.tether-gradle-plugin for how libraries are found.");
	}
}
