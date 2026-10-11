package com.serilum.tether.functions;

import com.serilum.tether.data.ClassRenames;
import com.serilum.tether.data.LibraryJars;
import org.gradle.api.GradleException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class RenameFunctions {
	public static ClassRenames getClassRenames(String loader, List<LibraryJars> libraryJarsList) {
		ClassRenames renames = new ClassRenames();

		for (LibraryJars libraryJars : libraryJarsList) {
			List<String> commonClasses = getClassNames(libraryJars.commonJar.getSingleFile());
			HashSet<String> mergedClasses = new HashSet<>(getClassNames(libraryJars.mergedJar.getSingleFile()));

			String packageFolder = "/" + libraryJars.modId + "/";
			for (String commonClass : commonClasses) {
				int packageIndex = commonClass.indexOf(packageFolder);
				if (packageIndex < 0) {
					continue;
				}

				String root = commonClass.substring(0, packageIndex + packageFolder.length());
				String splitClass = commonClass.substring(0, packageIndex + 1) + libraryJars.modId + "_common_" + loader + "/" + commonClass.substring(root.length());
				// Only rename what the released jar really has, so a library that isn't split is left alone.
				if (!mergedClasses.contains(splitClass)) {
					continue;
				}

				renames.internalNames.put(commonClass, splitClass);
				if (!renames.roots.contains(root)) {
					renames.roots.add(root);
				}
			}
		}

		return renames;
	}

	public static String renameReferences(String value, ClassRenames renames) {
		String renamedValue = renameReferences(value, renames, '/');
		return renameReferences(renamedValue, renames, '.');
	}

	private static String renameReferences(String value, ClassRenames renames, char separator) {
		String renamedValue = value;

		for (String root : renames.roots) {
			String prefix = root.replace('/', separator);

			int start = renamedValue.indexOf(prefix);
			while (start >= 0) {
				int end = start + prefix.length();
				while (end < renamedValue.length() && isClassNameCharacter(renamedValue.charAt(end), separator)) {
					end++;
				}

				String internalName = renamedValue.substring(start, end).replace(separator, '/');
				String splitName = renames.internalNames.get(internalName);
				if (splitName == null) {
					start = renamedValue.indexOf(prefix, end);
					continue;
				}

				String replacement = splitName.replace('/', separator);
				renamedValue = renamedValue.substring(0, start) + replacement + renamedValue.substring(end);
				start = renamedValue.indexOf(prefix, start + replacement.length());
			}
		}

		return renamedValue;
	}

	private static boolean isClassNameCharacter(char character, char separator) {
		return character == separator || Character.isJavaIdentifierPart(character);
	}

	private static List<String> getClassNames(File jarFile) {
		List<String> classNames = new ArrayList<>();

		try (ZipFile zipFile = new ZipFile(jarFile)) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				String name = entries.nextElement().getName();
				if (name.endsWith(".class")) {
					classNames.add(name.substring(0, name.length() - ".class".length()));
				}
			}
		}
		catch (IOException ex) {
			throw new GradleException("[Tether] Unable to read " + jarFile.getPath() + ".", ex);
		}

		return classNames;
	}
}
