package net.sf.xsltmp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Perform XSL transformation of multiple source files into a single destination
 * file.
 * <p>
 * The names of the multiple source files are passed into the template as a
 * comma-separated list in a parameter named 'source-file-names'. In the
 * template, define a global template parameter as follows:
 * <code>&lt;xsl:param name="source-file-names" /></code>. Then the content of
 * the files can be loaded as follows:
 * <code>&lt;xsl:variable name="source-files" select="document(tokenize($source-file-names,','))" /></code>.
 * <p>
 * From the XSL engine point of view, it is then in fact a single, 1:1
 * transformation: a single source file is transformed into a single destination
 * file.
 * <p>
 * The content of the single source file can then be enriched in the template by
 * the multiple source files.
 * 
 * @goal many-to-one
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class ManyToOneMojo extends FromManyBase {

	/**
	 * The single source file to be enriched by the template.
	 * <p>
	 * If not specified, a default source file is generated and used
	 * automatically, with the following content: <code>&lt;root/></code>
	 * 
	 * @parameter
	 */
	private File srcFile;

	/**
	 * The destination file.
	 * 
	 * @parameter
	 * @required
	 */
	private File destFile;

	private boolean shouldRun = false;

	// Standard getters and setters for the properties

	public File getSrcFile() {
		return srcFile;
	}

	public void setSrcFile(File srcFile) {
		this.srcFile = srcFile;
	}

	public File getDestFile() {
		return destFile;
	}

	public void setDestFile(File destFile) {
		this.destFile = destFile;
	}

	// Mojo implementation

	protected String getMojoName() {
		return "many-to-one";
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			if (!verifyXsltFileExist())
				return;
			if (!verifySrcDirExist())
				return;
			ensureDestFileDirExists(getDestFile());

			storeSourceFileNamesInParam();
			File amendedSrcFile;
			if (doesSrcFileExists()) {
				amendedSrcFile = getSrcFile();
				shouldRun |= hasChanged(amendedSrcFile);
			} else {
				getLog().info("Source file does not exist, using default.");
				amendedSrcFile = getDefaultFile();
			}
			if (shouldRun) {
				logExecution(getSrcFile());
				getTransformer().transform(new StreamSource(amendedSrcFile),
						new StreamResult(getDestFile()));
			} else {
				getLog().info("No sources to process.");
			}
		} catch (MojoFailureException mfe) {
			throw mfe;
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	// Private helper methods

	private File getDefaultFile() throws MojoFailureException {
		File defaultDestDir = new File(getProject().getBuild().getDirectory(),
				DEFAULT_DEST_DIR);
		File empty = new File(defaultDestDir, DEFAULT_EMPTY_FILENAME);
		if (!empty.exists())
			createDefaultFile(empty);
		return empty;
	}

	private void createDefaultFile(File file) throws MojoFailureException {
		file.getParentFile().mkdirs();
		try {
			FileWriter w = new FileWriter(file);
			w.write(DEFAULT_EMPTY_FILE_CONTENTS);
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new MojoFailureException("Cannot create default source, "
					+ e.getMessage(), e);
		}
	}

	private void storeSourceFileNamesInParam() throws MojoFailureException {
		try {
			boolean xslFileChanged = hasChanged(getXslFile());
			String srcDirPath = getSrcDir().getCanonicalPath();
			StringBuilder b = new StringBuilder();
			String[] sourceFileNames = getSourceFiles();
			for (int i = 0; i < sourceFileNames.length; i++) {
				File srcFile = getSourceFile(sourceFileNames[i]);
				if (!xslFileChanged && !hasChanged(srcFile)) {
					getLog().debug("File skipped: " + srcFile);
					continue;
				}
				shouldRun = true;
				String srcFilePath = srcFile.getCanonicalPath();
				if (!(srcFilePath.startsWith(srcDirPath)))
					throw new MojoFailureException(
							"Source file not within source directory: "
									+ srcFilePath);
				srcFilePath = srcFilePath.substring(srcDirPath.length() + 1)
						.replace('\\', '/');
				b.append(srcFilePath);
				if (i < sourceFileNames.length - 1)
					b.append(',');
			}
			String names = b.toString();
			getParameters().put("source-file-names", names);
			getLog().info("Stored source-file-names param: " + names);
		} catch (IOException e) {
			throw new MojoFailureException("Cannot read canonical file path", e);
		}
	}

	private boolean doesSrcFileExists() {
		return (null != getSrcFile() && getSrcFile().exists());
	}

}
