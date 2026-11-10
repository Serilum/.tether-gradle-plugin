package com.serilum.tether.functions;

import com.serilum.tether.actions.UnlinkRunModsAction;
import com.serilum.tether.data.ClassRenames;
import com.serilum.tether.data.Library;
import org.gradle.api.Project;
import org.gradle.api.tasks.JavaExec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RunModsFunctions {
	private static final String[] PACKAGE_ROOTS = { "com/natamus/", "com/serilum/" };
	private static final String ALWAYS_UNLINKED_LIBRARY = "collective";

	public static void unlinkRunMods(Project project, String loader, List<Library> libraries) {
		if (loader.equals("common")) {
			return;
		}

		UnlinkRunModsAction unlinkRunModsAction = new UnlinkRunModsAction(getUnlinkRenames(loader, libraries));
		project.getTasks().withType(JavaExec.class).configureEach(task -> {
			if (task.getName().startsWith("run")) {
				task.doFirst(unlinkRunModsAction);
			}
		});
	}

	public static ClassRenames getUnlinkRenames(String loader, List<Library> libraries) {
		List<String> modIds = new ArrayList<>();
		modIds.add(ALWAYS_UNLINKED_LIBRARY);
		for (Library library : libraries) {
			if (!modIds.contains(library.modId)) {
				modIds.add(library.modId);
			}
		}

		ClassRenames renames = new ClassRenames();
		for (String modId : modIds) {
			for (String packageRoot : PACKAGE_ROOTS) {
				renames.packageNames.put(packageRoot + modId + "_common_" + loader + "/", packageRoot + modId + "/");
			}
		}

		return renames;
	}

	public static File getGameFolder(JavaExec task) {
		List<String> args = task.getArgs();
		int gameDirIndex = args.indexOf("--gameDir");
		if (gameDirIndex < 0 || gameDirIndex + 1 >= args.size()) {
			return task.getWorkingDir();
		}

		File gameFolder = new File(args.get(gameDirIndex + 1));
		if (gameFolder.isAbsolute()) {
			return gameFolder;
		}

		return new File(task.getWorkingDir(), gameFolder.getPath());
	}
}
