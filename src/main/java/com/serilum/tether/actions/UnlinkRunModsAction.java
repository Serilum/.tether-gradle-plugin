package com.serilum.tether.actions;

import com.serilum.tether.data.ClassRenames;
import com.serilum.tether.functions.JarFunctions;
import com.serilum.tether.functions.RunModsFunctions;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.tasks.JavaExec;

import java.io.File;

public class UnlinkRunModsAction implements Action<Task> {
	private ClassRenames renames;

	public UnlinkRunModsAction(ClassRenames renames) {
		this.renames = renames;
	}

	@Override
	public void execute(Task task) {
		File modsFolder = new File(RunModsFunctions.getGameFolder((JavaExec)task), "mods");
		File[] modFiles = modsFolder.listFiles();
		if (modFiles == null) {
			return;
		}

		for (File modFile : modFiles) {
			String fileName = modFile.getName();
			if (!modFile.isFile() || !fileName.endsWith(".jar")) {
				continue;
			}

			int renamedFiles = JarFunctions.linkJar(modFile, renames);
			if (renamedFiles == 0) {
				continue;
			}

			task.getLogger().lifecycle("[Tether] Unlinked " + fileName + " in " + modsFolder.getPath() + " from the released libraries, so it runs in this dev client (" + renamedFiles + " files updated).");
		}
	}
}
