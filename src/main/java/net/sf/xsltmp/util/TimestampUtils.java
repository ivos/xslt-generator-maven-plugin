package net.sf.xsltmp.util;

import java.io.File;

import net.sf.xsltmp.XsltGeneratorConstants;

import org.apache.maven.project.MavenProject;

public class TimestampUtils implements XsltGeneratorConstants {

	private final File baseDir;

	public TimestampUtils(MavenProject project) {
		baseDir = new File(project.getBuild().getDirectory(), BASE_DIR);
	}

	public File getTimestampDir() {
		return baseDir;
	}

	public File getTimestampFile() {
		return new File(baseDir, TIMESTAMP_FILENAME);
	}

	public File getPreparedTimestampFile() {
		return new File(baseDir, PREPARED_TIMESTAMP_FILENAME);
	}

}
