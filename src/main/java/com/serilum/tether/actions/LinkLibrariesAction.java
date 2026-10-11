package com.serilum.tether.actions;

import com.serilum.tether.data.ClassRenames;
import com.serilum.tether.data.LibraryJars;
import com.serilum.tether.functions.JarFunctions;
import com.serilum.tether.functions.RenameFunctions;
import org.gradle.api.Action;
import org.gradle.api.Task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LinkLibrariesAction implements Action<Task> {
	private String loader;
	private List<LibraryJars> libraryJarsList;

	public LinkLibrariesAction(String loader, List<LibraryJars> libraryJarsList) {
		this.loader = loader;
		this.libraryJarsList = libraryJarsList;
	}

	@Override
	public void execute(Task task) {
		ClassRenames renames = RenameFunctions.getClassRenames(loader, libraryJarsList);
		if (renames.internalNames.isEmpty()) {
			return;
		}

		List<String> libraryNames = new ArrayList<>();
		for (LibraryJars libraryJars : libraryJarsList) {
			libraryNames.add(libraryJars.modId);
		}

		for (File outputFile : task.getOutputs().getFiles()) {
			String fileName = outputFile.getName();
			if (!outputFile.isFile() || !fileName.endsWith(".jar") || fileName.endsWith("-sources.jar")) {
				continue;
			}

			int renamedFiles = JarFunctions.linkJar(outputFile, renames);
			if (renamedFiles == 0) {
				continue;
			}

			task.getLogger().lifecycle("[Tether] Linked " + fileName + " to the released " + String.join(", ", libraryNames) + " (" + renamedFiles + " files updated).");
		}
	}
}
