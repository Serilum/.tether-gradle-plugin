package com.serilum.tether.functions;

import com.serilum.tether.data.Library;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;

public class DependencyFunctions {
	public static void addLibrary(Project project, String loader, Library library) {
		String minecraftVersion = ProjectFunctions.getMinecraftVersion(project);

		String loaderArtifact = getArtifactNotation(project, library, "-" + loader);
		// The merged jar is only for compiling, it has the _common_ packages a split release build imports.
		String mergedArtifact = getArtifactNotation(project, library, "");

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
			// 1.20.1 applies ForgeGradle after the plugins block, so fg.deobf only exists once it's there.
			project.getPluginManager().withPlugin("net.minecraftforge.gradle", forgeGradlePlugin -> {
				addDependency(project, "runtimeOnly", deobf(project, loaderArtifact));
				addDependency(project, "compileOnly", deobf(project, loaderArtifact));
				addDependency(project, "compileOnly", deobf(project, mergedArtifact));
				// Forge compiles Common's source into its tests as well, which fails without the library.
				addDependency(project, "testCompileOnly", deobf(project, loaderArtifact));
			});
			return;
		}

		addDependency(project, "runtimeOnly", loaderArtifact);
		addDependency(project, "compileOnly", loaderArtifact);
		addDependency(project, "compileOnly", mergedArtifact);
	}

	public static String getArtifactNotation(Project project, Library library, String artifactSuffix) {
		String version = ProjectFunctions.getMinecraftVersion(project) + "-" + library.version;
		return library.mavenGroup + ":" + library.modId + artifactSuffix + ":" + version;
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
			throw new GradleException("[Tether] ForgeGradle is applied, but its 'fg' extension is missing.");
		}

		try {
			return forgeGradle.getClass().getMethod("deobf", Object.class).invoke(forgeGradle, artifact);
		}
		catch (Exception ex) {
			throw new GradleException("[Tether] Unable to call fg.deobf for " + artifact + ".", ex);
		}
	}
}
