package net.sf.xsltmp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * A base class for mojos with multiple destination files.
 */
public abstract class ManyToManyBase extends FromManyBase {

	/**
	 * The destination directory. The output files are written based on this
	 * directory.
	 * 
	 * @parameter 
	 *            expression="${project.build.directory}/generated-xml/xslt-generator/"
	 */
	private File destDir;

	/**
	 * Where should the destination directory be added. Valid values are:
	 * sources, resources, test-sources, test-resources.
	 * <p>
	 * This is a shortcut to avoid configuring the build-helper-maven-plugin.
	 * <p>
	 * When adding to resources or test-resources, all .java files are excluded
	 * by default.
	 * 
	 * @parameter
	 */
	private String addDestDirTo;

	// Standard getters and setters for the properties

	public File getDestDir() {
		return destDir;
	}

	public void setDestDir(File destDir) {
		this.destDir = destDir;
	}

	public String getAddDestDirTo() {
		return addDestDirTo;
	}

	public void setAddDestDirTo(String addDestDirTo) {
		this.addDestDirTo = addDestDirTo;
	}

	// Standard implementation of the transformation algorithm for multiple
	// destination files.

	/**
	 * The main MOJO execution method.
	 * 
	 * Perform a generic XSL transformation algorithm. The mojo-specific parts
	 * are implemented in called abstract methods in the class descendants.
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		boolean didRun = false;
		try {
			if (!verifyXsltFileExist())
				return;
			if (!verifySrcDirExist())
				return;
			ensureDestDirExists();

			boolean xslFileChanged = hasChanged(getXslFile());
			String[] sourceFileNames = getSourceFiles();
			for (int i = 0; i < sourceFileNames.length; i++) {
				File srcFile = getSourceFile(sourceFileNames[i]);
				File destFile = getDestFile(sourceFileNames[i]);
				if (!xslFileChanged
						&& (shouldSkip(srcFile, destFile) || !hasChanged(srcFile))) {
					if (getLog().isDebugEnabled())
						getLog().debug("File skipped: " + srcFile);
					continue;
				}
				didRun = true;
				ensureDestFileDirExists(destFile);
				logExecution(srcFile);
				getTransformer().transform(new StreamSource(srcFile),
						new StreamResult(destFile));
				cleanAfterFileTransformation(destFile);
			}
			if (!didRun)
				getLog().info("No sources to process.");
			addDestDir();
		} catch (MojoFailureException mfe) {
			throw mfe;
		} catch (Exception e) {
			e.printStackTrace();
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	// The abstract methods which defer implementation of the specifics of the
	// transformation algorithm to descendants.

	/**
	 * Return the destination file for a given source file name.
	 * 
	 * @param sourceFileName
	 * @return File
	 */
	protected abstract File getDestFile(String sourceFileName);

	/**
	 * Return true iff the transformation from the given source file to the
	 * given destination file should be skipped.
	 * 
	 * @param srcFile
	 * @param destFile
	 * @return boolean
	 */
	protected abstract boolean shouldSkip(File srcFile, File destFile);

	/**
	 * Perform any clean-up after a source file has been transformed into the
	 * given destination file.
	 * 
	 * @param destFile
	 */
	protected void cleanAfterFileTransformation(File destFile) {
	}

	// Helper methods

	protected void ensureDestDirExists() throws MojoFailureException {
		if (!getDestDir().exists()) {
			boolean dirCreationResult = getDestDir().mkdirs();
			if (!dirCreationResult) {
				throw new MojoFailureException(
						"Destination directory structure could not be initialised. "
								+ "Failed to create directory: " + getDestDir());
			}
		}
	}

	/**
	 * Add the destination directory to the project.
	 */
	protected void addDestDir() {
		if ("sources".equals(getAddDestDirTo())) {
			getProject().addCompileSourceRoot(getDestDir().getAbsolutePath());
			logAddedDestDir();
		}
		if ("test-sources".equals(getAddDestDirTo())) {
			getProject().addTestCompileSourceRoot(
					getDestDir().getAbsolutePath());
			logAddedDestDir();
		}
		if ("resources".equals(getAddDestDirTo())) {
			getProject().addResource(getDestDirResource());
			logAddedDestDir();
		}
		if ("test-resources".equals(getAddDestDirTo())) {
			getProject().addTestResource(getDestDirResource());
			logAddedDestDir();
		}
	}

	private Resource getDestDirResource() {
		Resource resource = new Resource();
		resource.setDirectory(getDestDir().getAbsolutePath());
		List excludes = new ArrayList();
		excludes.add("**/*.java");
		resource.setExcludes(excludes);
		return resource;
	}

	private void logAddedDestDir() {
		getLog().info(
				"Added to " + getAddDestDirTo() + " destDir: "
						+ getDestDir().getAbsolutePath());
	}

}
