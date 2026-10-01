package com.serilum.tether.functions;

import com.serilum.tether.data.Library;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;

public class DependencyFunctions {
	public static void addLibrary(Project project, String loader, Library library) {
		String libraryVersion = ProjectFunctions.getProperty(project, library.versionProperty);
		if (libraryVersion.equals("")) {
			return;
		}

		String minecraftVersion = ProjectFunctions.getMinecraftVersion(project);
		String version = minecraftVersion + "-" + libraryVersion;

		String loaderArtifact = library.mavenGroup + ":" + library.modId + "-" + loader + ":" + version;
		String mergedArtifact = library.mavenGroup + ":" + library.modId + ":" + version;

		if (loader.equals("common")) {
			addDependency(project, "compileOnly", loaderArtifact);
			return;
		}

		if (loader.equals("fabric")) {
			if (ProjectFunctions.isObfuscated(minecraftVersion)) {
				addDependency(project, "modImplementation", loaderArtifact);
				addDependency(project, "modCompileOnly", mergedArtifact);
			}
			else {
				addDependency(project, "implementation", loaderArtifact);
				addDependency(project, "compileOnly", mergedArtifact);
			}
			return;
		}

		if (loader.equals("forge") && ProjectFunctions.forgeUsesDeobf(minecraftVersion)) {
			addDependency(project, "runtimeOnly", deobf(project, loaderArtifact));
			addDependency(project, "compileOnly", deobf(project, loaderArtifact));
			addDependency(project, "compileOnly", deobf(project, mergedArtifact));
			return;
		}

		addDependency(project, "runtimeOnly", loaderArtifact);
		addDependency(project, "compileOnly", loaderArtifact);
		addDependency(project, "compileOnly", mergedArtifact);
	}

	private static void addDependency(Project project, String configurationName, Object notation) {
		project.getConfigurations().configureEach(configuration -> {
			if (!configuration.getName().equals(configurationName)) {
				return;
			}

			if (notation instanceof Dependency dependency) {
				configuration.getDependencies().add(dependency);
				return;
			}

			configuration.getDependencies().add(project.getDependencies().create(notation));
		});
	}

	private static Object deobf(Project project, String artifact) {
		Object forgeGradle = project.getExtensions().findByName("fg");
		if (forgeGradle == null) {
			throw new GradleException("[Tether] ForgeGradle's 'fg' extension is missing. Apply com.serilum.tether after net.minecraftforge.gradle.");
		}

		try {
			return forgeGradle.getClass().getMethod("deobf", Object.class).invoke(forgeGradle, artifact);
		}
		catch (Exception ex) {
			throw new GradleException("[Tether] Unable to call fg.deobf for " + artifact + ".", ex);
		}
	}
}
