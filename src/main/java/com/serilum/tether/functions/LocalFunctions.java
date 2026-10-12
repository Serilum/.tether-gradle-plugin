package com.serilum.tether.functions;

import com.serilum.tether.TetherLocal;
import com.serilum.tether.data.Library;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencySubstitutions;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ExtraPropertiesExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class LocalFunctions {
	private static final String[] LOADER_FOLDERS = { "Common", "Fabric", "Forge", "NeoForge" };
	private static final String LOCAL_LOADERS_PROPERTY = "tetherLocalLoaders";

	private static final Logger LOGGER = Logging.getLogger(TetherLocal.class);

	public static boolean isEnabled(Settings settings) {
		return SettingsFunctions.getProperty(settings, "tether_local").equals("true") && SettingsFunctions.isToggledOn(settings, "tether_dependencies");
	}

	public static void includeLocalLibraries(Settings settings) {
		String modId = SettingsFunctions.getProperty(settings, "mod_id");
		String minecraftVersion = SettingsFunctions.getMinecraftVersion(settings);
		List<String> localLoaders = new ArrayList<>();

		for (Library library : LibraryFunctions.getLibraries(settings.getRootDir(), modId, minecraftVersion)) {
			File localFolder = getLocalFolder(settings.getRootDir(), library);
			String librarySettings = readSettings(localFolder);
			if (!librarySettings.contains("com.serilum.tether.local")) {
				LOGGER.lifecycle("[Tether] tether_local is on, but there's no " + localFolder.getName() + " folder with Tether's local plugin next to this one. Using the released " + library.modId + " instead.");
				continue;
			}

			List<String> loaderFolders = getIncludedLoaderFolders(localFolder, librarySettings);
			for (String loaderFolder : loaderFolders) {
				localLoaders.add(getLocalLoaderKey(localFolder, loaderFolder));
			}

			settings.includeBuild(localFolder, includedBuild -> includedBuild.dependencySubstitution(substitutions -> substituteLoaders(substitutions, library, loaderFolders)));
			LOGGER.lifecycle("[Tether] Using the " + library.modId + " source from " + localFolder.getName() + ".");
		}

		settings.getGradle().getExtensions().getExtraProperties().set(LOCAL_LOADERS_PROPERTY, localLoaders);
	}

	public static boolean isLocal(Project project, Library library) {
		ExtraPropertiesExtension extraProperties = project.getGradle().getExtensions().getExtraProperties();
		if (!extraProperties.has(LOCAL_LOADERS_PROPERTY)) {
			return false;
		}

		List<?> localLoaders = (List<?>)extraProperties.get(LOCAL_LOADERS_PROPERTY);
		return localLoaders.contains(getLocalLoaderKey(getLocalFolder(project.getRootDir(), library), project.getName()));
	}

	public static void addLocalLibrary(Project project, String loader, Library library) {
		DependencyFunctions.addDependency(project, "compileOnly", getLocalDependency(project, loader, library));
		if (loader.equals("common")) {
			return;
		}

		DependencyFunctions.addDependency(project, "runtimeOnly", getLocalDependency(project, loader, library));
		DependencyFunctions.addDependency(project, "testCompileOnly", getLocalDependency(project, loader, library));
	}

	public static List<Library> getReleasedLibraries(Project project, List<Library> libraries) {
		List<Library> releasedLibraries = new ArrayList<>();
		for (Library library : libraries) {
			if (!isLocal(project, library)) {
				releasedLibraries.add(library);
			}
		}

		return releasedLibraries;
	}

	public static String getCapability(String modId, String loader) {
		return "com.serilum.tether:" + modId + "-" + loader + "-local";
	}

	public static String getLocalGroup(String modId) {
		return "com.serilum.tether.local." + modId;
	}

	private static ModuleDependency getLocalDependency(Project project, String loader, Library library) {
		String loaderArtifact = DependencyFunctions.getArtifactNotation(project, library, "-" + loader);
		ModuleDependency dependency = (ModuleDependency)project.getDependencies().create(loaderArtifact);
		dependency.capabilities(capabilities -> capabilities.requireCapability(getCapability(library.modId, loader)));
		return dependency;
	}

	private static File getLocalFolder(File rootDir, Library library) {
		return new File(rootDir.getParentFile(), library.modId + "-" + library.minecraftVersion);
	}

	private static String getLocalLoaderKey(File localFolder, String loaderFolder) {
		return new File(localFolder, loaderFolder).toPath().toAbsolutePath().normalize().toString();
	}

	private static String readSettings(File localFolder) {
		File settingsFile = new File(localFolder, "settings.gradle");
		if (!settingsFile.isFile()) {
			return "";
		}

		try {
			return Files.readString(settingsFile.toPath());
		}
		catch (IOException ex) {
			throw new GradleException("[Tether] Unable to read " + settingsFile.getPath() + ".", ex);
		}
	}

	private static List<String> getIncludedLoaderFolders(File localFolder, String librarySettings) {
		List<String> loaderFolders = new ArrayList<>();
		for (String loaderFolder : LOADER_FOLDERS) {
			boolean included = librarySettings.contains("\"" + loaderFolder + "\"") || librarySettings.contains("'" + loaderFolder + "'");
			if (included && new File(localFolder, loaderFolder).isDirectory()) {
				loaderFolders.add(loaderFolder);
			}
		}

		return loaderFolders;
	}

	private static void substituteLoaders(DependencySubstitutions substitutions, Library library, List<String> loaderFolders) {
		for (String loaderFolder : loaderFolders) {
			String loader = loaderFolder.toLowerCase();
			substitutions.substitute(substitutions.module(library.mavenGroup + ":" + library.modId + "-" + loader)).using(substitutions.project(":" + loaderFolder));
		}
	}
}
