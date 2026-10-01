package com.serilum.tether.functions;

import org.gradle.api.GradleException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class SourceFunctions {
	public static List<String> getImportedNatamusPackages(File rootDir) {
		List<String> packages = new ArrayList<>();

		for (String moduleName : new String[]{ "Common", "Fabric", "Forge", "NeoForge" }) {
			File javaDir = new File(rootDir, moduleName + File.separator + "src" + File.separator + "main" + File.separator + "java");
			collectImports(javaDir, packages);
		}

		return packages;
	}

	private static void collectImports(File file, List<String> packages) {
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			if (children == null) {
				return;
			}

			for (File child : children) {
				collectImports(child, packages);
			}
			return;
		}

		if (!file.getName().endsWith(".java")) {
			return;
		}

		List<String> lines;
		try {
			lines = Files.readAllLines(file.toPath());
		}
		catch (IOException ex) {
			throw new GradleException("[Tether] Unable to read " + file.getPath() + ".", ex);
		}

		for (String rawLine : lines) {
			String line = rawLine.strip().replace("import static ", "import ");
			if (!line.startsWith("import com.natamus.")) {
				continue;
			}

			String packageName = line.substring("import com.natamus.".length());
			if (!packageName.contains(".")) {
				continue;
			}

			packageName = packageName.substring(0, packageName.indexOf("."));
			if (!packages.contains(packageName)) {
				packages.add(packageName);
			}
		}
	}
}
