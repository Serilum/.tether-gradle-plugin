package com.serilum.tether.data;

import org.gradle.api.file.FileCollection;

public class LibraryJars {
	public String modId;
	public FileCollection commonJar;
	public FileCollection mergedJar;

	public LibraryJars(String modId, FileCollection commonJar, FileCollection mergedJar) {
		this.modId = modId;
		this.commonJar = commonJar;
		this.mergedJar = mergedJar;
	}
}
