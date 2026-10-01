package com.serilum.tether.functions;

import org.gradle.api.GradleException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class SourceFunctions {
	private static final String[] IMPORT_PREFIXES = { "import com.serilum.", "import com.natamus." };
	private static final String[] BUILD_SUFFIXES = { "_common_fabric", "_common_forge", "_common_neoforge" };

	public static List<String> getImportedLibraryPackages(File rootDir) {
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
			String packageName = getImportedPackage(line);
			if (packageName.equals("") || packages.contains(packageName)) {
				continue;
			}

			packages.add(packageName);
		}
	}

	private static String getImportedPackage(String line) {
		for (String prefix : IMPORT_PREFIXES) {
			if (!line.startsWith(prefix)) {
				continue;
			}

			String packageName = line.substring(prefix.length());
			if (!packageName.contains(".")) {
				return "";
			}

			return withoutBuildSuffix(packageName.substring(0, packageName.indexOf(".")));
		}
		return "";
	}

	private static String withoutBuildSuffix(String packageName) {
		for (String suffix : BUILD_SUFFIXES) {
			if (packageName.endsWith(suffix)) {
				return packageName.substring(0, packageName.length() - suffix.length());
			}
		}
		return packageName;
	}
}
