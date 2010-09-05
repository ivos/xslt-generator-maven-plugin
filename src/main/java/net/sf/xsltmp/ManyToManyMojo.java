package net.sf.xsltmp;

import java.io.File;

/**
 * Perform XSL transformation of multiple source files with destination files
 * created 1:1.
 * <p>
 * Each source file is transformed into a single destination file, preserving
 * the directory structure. The destination directory tree is created based on
 * the source directory tree.
 * <p>
 * Files with up-to-date timestamp are skipped.
 * 
 * @goal many-to-many
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class ManyToManyMojo extends ManyToManyBase {

	/**
	 * A regular expression that will match part of the XML file name for
	 * replacement.
	 * 
	 * @parameter
	 */
	private String fileNameRegex;

	/**
	 * The replacement for the matched regular expression of the XML file name.
	 * 
	 * @parameter
	 */
	private String fileNameReplacement;

	// Standard getters and setters for the properties

	public String getFileNameRegex() {
		return fileNameRegex;
	}

	public void setFileNameRegex(String fileNameRegex) {
		this.fileNameRegex = fileNameRegex;
	}

	public String getFileNameReplacement() {
		return fileNameReplacement;
	}

	public void setFileNameReplacement(String fileNameReplacement) {
		this.fileNameReplacement = fileNameReplacement;
	}

	// Hook methods implementation

	protected String getMojoName() {
		return "many-to-many";
	}

	protected File getDestFile(String sourceFile) {
		String destFileName = sourceFile;
		if (getFileNameRegex() != null && getFileNameReplacement() != null) {
			destFileName = destFileName.replaceAll(getFileNameRegex(),
					getFileNameReplacement());
		}
		return new File(getDestDir(), destFileName);
	}

	protected boolean shouldSkip(File srcFile, File destFile) {
		return destFile.exists()
				&& srcFile.lastModified() < destFile.lastModified();
	}

}
