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

	private static final String[] MODULE_NAMES = { "Common", "Fabric", "Forge", "NeoForge" };

	public static List<String> getImportedLibraryPackages(File rootDir) {
		List<String> packages = new ArrayList<>();

		for (String moduleName : MODULE_NAMES) {
			collectImports(getJavaDir(rootDir, moduleName), packages);
		}

		return packages;
	}

	public static boolean hasSource(File rootDir) {
		for (String moduleName : MODULE_NAMES) {
			if (containsJavaFile(getJavaDir(rootDir, moduleName))) {
				return true;
			}
		}

		return false;
	}

	public static boolean isSplitSource(File projectDir, String loader) {
		// A split source is one of my own release builds. Its imports already use the _common_ packages, so the jar is left alone.
		File javaDir = new File(projectDir, "src" + File.separator + "main" + File.separator + "java");
		return containsFolderEndingWith(javaDir, "_common_" + loader);
	}

	private static File getJavaDir(File rootDir, String moduleName) {
		return new File(rootDir, moduleName + File.separator + "src" + File.separator + "main" + File.separator + "java");
	}

	private static boolean containsJavaFile(File folder) {
		File[] children = folder.listFiles();
		if (children == null) {
			return false;
		}

		for (File child : children) {
			if (child.getName().endsWith(".java") || containsJavaFile(child)) {
				return true;
			}
		}

		return false;
	}

	private static boolean containsFolderEndingWith(File folder, String suffix) {
		File[] children = folder.listFiles();
		if (children == null) {
			return false;
		}

		for (File child : children) {
			if (!child.isDirectory()) {
				continue;
			}

			if (child.getName().endsWith(suffix) || containsFolderEndingWith(child, suffix)) {
				return true;
			}
		}

		return false;
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
