package com.serilum.tether;

import com.serilum.tether.data.Library;
import com.serilum.tether.functions.DependencyFunctions;
import com.serilum.tether.functions.LibraryFunctions;
import com.serilum.tether.functions.LinkFunctions;
import com.serilum.tether.functions.ProjectFunctions;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.List;

public class Tether implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		if (!ProjectFunctions.isToggledOn(project, "tether_enabled")) {
			return;
		}

		String loader = ProjectFunctions.getLoader(project);
		if (loader.equals("")) {
			return;
		}

		boolean addDependencies = ProjectFunctions.isToggledOn(project, "tether_dependencies");
		boolean linkJar = ProjectFunctions.isToggledOn(project, "tether_link");
		if (!addDependencies && !linkJar) {
			return;
		}

		List<Library> libraries = LibraryFunctions.getLibraries(project);
		if (libraries.isEmpty()) {
			return;
		}

		project.getRepositories().maven(repository -> repository.setUrl("https://maven.serilum.com/"));

		if (addDependencies) {
			for (Library library : libraries) {
				DependencyFunctions.addLibrary(project, loader, library);
			}
		}

		if (linkJar) {
			LinkFunctions.linkBuiltJar(project, loader, libraries);
		}
	}
}
