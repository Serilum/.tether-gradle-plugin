package com.serilum.tether.functions;

import org.gradle.api.Project;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;

import java.util.List;

public class LocalElementsFunctions {
	public static void addLocalElements(Gradle gradle) {
		gradle.allprojects(project -> {
			String loader = ProjectFunctions.getLoader(project);
			if (loader.equals("")) {
				return;
			}

			// A group of its own, set before the build scripts run, so Gradle doesn't mix these up with the mod's com.serilum:Fabric and such.
			project.setGroup(LocalFunctions.getLocalGroup(ProjectFunctions.getProperty(project, "mod_id")));

			project.getPluginManager().withPlugin("java", javaPlugin -> addLocalElements(project, loader));
		});
	}

	private static void addLocalElements(Project project, String loader) {
		String modId = ProjectFunctions.getProperty(project, "mod_id");
		SourceSet main = project.getExtensions().getByType(SourceSetContainer.class).getByName("main");

		TaskProvider<Jar> localJar = project.getTasks().register("tetherLocalJar", Jar.class, jar -> {
			jar.from(main.getOutput());
			jar.getArchiveClassifier().set("tether-local");
			jar.getDestinationDirectory().set(project.getLayout().getBuildDirectory().dir("tether"));
			jar.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
			jar.manifest(manifest -> manifest.from(project.getTasks().named("jar", Jar.class).get().getManifest()));
			// MixinGradle only adds MixinConfigs to the jar's manifest once this task has run (1.20.1 Forge).
			jar.dependsOn(project.provider(() -> getMixinTasks(project)));
		});

		ObjectFactory objects = project.getObjects();
		project.getConfigurations().create("tetherLocalElements", configuration -> {
			configuration.setCanBeConsumed(true);
			configuration.setCanBeResolved(false);
			configuration.attributes(attributes -> {
				attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.JAVA_RUNTIME));
				attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.class, Category.LIBRARY));
				attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, LibraryElements.JAR));
				attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.class, Bundling.EXTERNAL));
			});
			configuration.getOutgoing().capability(LocalFunctions.getCapability(modId, loader) + ":" + project.getVersion());
			configuration.getOutgoing().artifact(localJar);
		});
	}

	private static List<Object> getMixinTasks(Project project) {
		if (!project.getTasks().getNames().contains("addMixinsToJar")) {
			return List.of();
		}

		return List.of(project.getTasks().named("addMixinsToJar"));
	}
}
