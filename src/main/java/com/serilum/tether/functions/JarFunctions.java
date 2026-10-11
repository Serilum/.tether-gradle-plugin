package com.serilum.tether.functions;

import com.serilum.tether.data.ClassRenames;
import org.gradle.api.GradleException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class JarFunctions {
	private static final String SERVICES_FOLDER = "META-INF/services/";

	public static int linkJar(File jarFile, ClassRenames renames) {
		List<String> names = new ArrayList<>();
		List<ZipEntry> originalEntries = new ArrayList<>();
		List<byte[]> contents = new ArrayList<>();
		int renamedFiles = 0;

		try (ZipFile zipFile = new ZipFile(jarFile)) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String name = entry.getName();
				byte[] content = zipFile.getInputStream(entry).readAllBytes();

				byte[] renamedContent = renameContent(name, content, renames);
				String renamedName = renameEntryName(name, renames);
				if (renamedContent != content || !renamedName.equals(name)) {
					renamedFiles++;
				}

				names.add(renamedName);
				originalEntries.add(entry);
				contents.add(renamedContent);
			}
		}
		catch (IOException ex) {
			throw new GradleException("[Tether] Unable to read " + jarFile.getPath() + ".", ex);
		}

		if (renamedFiles == 0) {
			return 0;
		}

		writeJar(jarFile, names, originalEntries, contents);
		return renamedFiles;
	}

	private static byte[] renameContent(String name, byte[] content, ClassRenames renames) throws IOException {
		if (name.endsWith(".class")) {
			return ClassFileFunctions.renameClasses(content, renames);
		}

		if (name.startsWith(SERVICES_FOLDER) && !name.endsWith("/")) {
			String text = new String(content, StandardCharsets.UTF_8);
			String renamedText = RenameFunctions.renameReferences(text, renames);
			if (renamedText.equals(text)) {
				return content;
			}

			return renamedText.getBytes(StandardCharsets.UTF_8);
		}

		return content;
	}

	private static String renameEntryName(String name, ClassRenames renames) {
		if (!name.startsWith(SERVICES_FOLDER) || name.endsWith("/")) {
			return name;
		}

		return SERVICES_FOLDER + RenameFunctions.renameReferences(name.substring(SERVICES_FOLDER.length()), renames);
	}

	private static void writeJar(File jarFile, List<String> names, List<ZipEntry> originalEntries, List<byte[]> contents) {
		File linkedFile = new File(jarFile.getPath() + ".tether");

		try (ZipOutputStream output = new ZipOutputStream(new FileOutputStream(linkedFile))) {
			for (int i = 0; i < names.size(); i++) {
				ZipEntry originalEntry = originalEntries.get(i);

				ZipEntry entry = new ZipEntry(names.get(i));
				entry.setTime(originalEntry.getTime());

				output.putNextEntry(entry);
				output.write(contents.get(i));
				output.closeEntry();
			}
		}
		catch (IOException ex) {
			boolean ignored = linkedFile.delete();
			throw new GradleException("[Tether] Unable to write " + linkedFile.getPath() + ".", ex);
		}

		try {
			Files.move(linkedFile.toPath(), jarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException ex) {
			throw new GradleException("[Tether] Unable to replace " + jarFile.getPath() + ".", ex);
		}
	}
}
