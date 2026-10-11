package com.serilum.tether.functions;

import com.serilum.tether.actions.LinkLibrariesAction;
import com.serilum.tether.data.Library;
import com.serilum.tether.data.LibraryJars;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LinkFunctions {
	public static void linkBuiltJar(Project project, String loader, List<Library> libraries) {
		if (loader.equals("common")) {
			return;
		}

		if (SourceFunctions.isSplitSource(project.getProjectDir(), loader)) {
			return;
		}

		List<LibraryJars> libraryJarsList = new ArrayList<>();
		for (Library library : libraries) {
			FileCollection commonJar = getArtifactFile(project, DependencyFunctions.getArtifactNotation(project, library, "-common"));
			FileCollection mergedJar = getArtifactFile(project, DependencyFunctions.getArtifactNotation(project, library, ""));
			libraryJarsList.add(new LibraryJars(library.modId, commonJar, mergedJar));
		}

		LinkLibrariesAction linkLibrariesAction = new LinkLibrariesAction(loader, libraryJarsList);
		project.afterEvaluate(evaluatedProject -> {
			String taskName = getFinalJarTaskName(evaluatedProject);
			evaluatedProject.getTasks().named(taskName).configure(task -> task.doLast(linkLibrariesAction));
		});
	}

	private static FileCollection getArtifactFile(Project project, String notation) {
		Configuration configuration = project.getConfigurations().detachedConfiguration(project.getDependencies().create(notation));
		configuration.setTransitive(false);
		return configuration;
	}

	private static String getFinalJarTaskName(Project project) {
		// On 1.20.1 Forge reobfJar isn't registered yet at this point, so jar is linked and reobfJar runs on that.
		Set<String> taskNames = project.getTasks().getNames();
		if (taskNames.contains("remapJar")) {
			return "remapJar";
		}

		if (taskNames.contains("reobfJar")) {
			return "reobfJar";
		}

		return "jar";
	}
}
