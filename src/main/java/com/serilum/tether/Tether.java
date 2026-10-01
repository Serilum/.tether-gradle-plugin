package com.serilum.tether;

import com.serilum.tether.data.Libraries;
import com.serilum.tether.data.Library;
import com.serilum.tether.functions.DependencyFunctions;
import com.serilum.tether.functions.ProjectFunctions;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class Tether implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		String loader = ProjectFunctions.getLoader(project);
		if (loader.equals("")) {
			return;
		}

		project.getRepositories().maven(repository -> repository.setUrl("https://maven.serilum.com/"));

		String modId = ProjectFunctions.getProperty(project, "mod_id");
		for (Library library : Libraries.libraries) {
			if (library.modId.equals(modId)) {
				continue;
			}

			DependencyFunctions.addLibrary(project, loader, library);
		}
	}
}
