package net.sf.xsltmp;

import java.io.File;

/**
 * Perform XSL transformation of multiple source files into destination files
 * created dynamically by the template.
 * <p>
 * For each source file, the template is supposed to create destination files
 * dynamically using the <code>xsl:result-document</code> command.
 * <p>
 * The default output of the template is directed to a file which is deleted
 * after processing.
 * 
 * @goal many-to-dynamic
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class ManyToDynamicMojo extends ManyToManyBase {

	protected String getMojoName() {
		return "many-to-dynamic";
	}

	protected File getDestFile(String sourceFile) {
		String destFileName = "null";
		return new File(getDestDir(), destFileName);
	}

	protected boolean shouldSkip(File srcFile, File destFile) {
		return false;
	}

	protected void cleanAfterFileTransformation(File destFile) {
		if (destFile.exists())
			destFile.delete();
	}

}
