package net.sf.seaf.mojo.xsltgenerator;

import java.io.File;
import java.util.Arrays;

import net.sf.seaf.mojo.util.DefaultURIResolver;

import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.StringUtils;

public abstract class FromManyBase extends XsltGeneratorBase {

	/**
	 * The source directory. The source files are read from here.
	 * 
	 * @parameter expression="${project.build.sourceDirectory}"
	 * @required
	 */
	private File srcDir;

	/**
	 * Which files from the source directory tree should be included in the
	 * transformation.
	 * <p>
	 * Specifies a fileset of source files in the comma- or space-separated list
	 * of patterns of files.
	 * 
	 * @parameter default-value="**\/*.xml"
	 */
	private String srcIncludes;

	/**
	 * Which files from the source directory tree should be excluded in the
	 * transformation.
	 * <p>
	 * Specifies a fileset of source files in the comma- or space-separated list
	 * of patterns of files.
	 * 
	 * @parameter
	 */
	private String srcExcludes;

	private File timestamp = null;

	// Standard getters and setters for the properties

	public File getSrcDir() {
		return srcDir;
	}

	public void setSrcDir(File srcDir) {
		this.srcDir = srcDir;
	}

	public String getSrcIncludes() {
		return srcIncludes;
	}

	public void setSrcIncludes(String srcIncludes) {
		this.srcIncludes = srcIncludes;
	}

	public String getSrcExcludes() {
		return srcExcludes;
	}

	public void setSrcExcludes(String srcExcludes) {
		this.srcExcludes = srcExcludes;
	}

	public File getTimestamp() {
		if (null == timestamp) {
			File timestampDir = new File(
					getProject().getBuild().getDirectory(), BASE_DIR);
			timestamp = new File(timestampDir, TIMESTAMP_FILENAME);
		}
		return timestamp;
	}

	// Shared helper methods

	protected boolean verifySrcDirExist() {
		if (!getSrcDir().exists()) {
			getLog().warn(
					"Generation not performed, source directory does not exist: "
							+ getSrcDir());
			return false;
		}
		return true;
	}

	protected String[] getSourceFiles() {
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(getSrcDir());
		scanner.setIncludes(StringUtils.split(getSrcIncludes(), ","));
		if (getSrcExcludes() != null) {
			scanner.setExcludes(StringUtils.split(getSrcExcludes(), ","));
		}
		scanner.scan();
		String[] sourceFiles = scanner.getIncludedFiles();
		// sort the file names to keep consistent order over multiple OS'es
		Arrays.sort(sourceFiles);
		if (0 == sourceFiles.length)
			getLog().info("No source files to process.");
		return sourceFiles;
	}

	protected File getSourceFile(String sourceFileName) {
		return new File(getSrcDir(), sourceFileName);
	}

	protected void ensureDestFileDirExists(File destFile)
			throws MojoFailureException {
		File parentDestFile = destFile.getParentFile();
		if (!parentDestFile.exists()) {
			boolean dirCreationResult = parentDestFile.mkdirs();
			if (!dirCreationResult) {
				throw new MojoFailureException(
						"Destination directory structure could not be initialised. "
								+ "Failed to create directory: "
								+ parentDestFile.getAbsolutePath());
			}
		}
	}

	protected boolean isUpToDate(File srcFile) {
		return getTimestamp().lastModified() > srcFile.lastModified();
	}

	protected void logExecution(File srcFile) {
		getLog().info("Running " + getLogPrefix() + " on srcFile: " + srcFile);
	}

	// Set up URIResolver from srcDir.
	public DefaultURIResolver getResolver() {
		if (null == resolver) {
			getLog().debug("Setting up SrcDirURIResolver");
			resolver = new SrcDirURIResolver(getSrcDir(), getLog(),
					getProject(), getHelper());
		}
		return resolver;
	}

}
