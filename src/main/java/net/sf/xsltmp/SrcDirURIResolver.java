package net.sf.xsltmp;

import java.io.File;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;

import net.sf.xsltmp.util.DefaultURIResolver;
import net.sf.xsltmp.util.UnArchiverHelper;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * URIResolver that resolves either from source directory, the received base
 * URI, as an absolute path, from project basedir or from classpath.
 * <p>
 * First tries to resolve from the source directory.
 * <p>
 * Then resolves from the other options, see {@link DefaultURIResolver} for
 * details.
 */
public class SrcDirURIResolver extends DefaultURIResolver {

	/**
	 * The source dir
	 */
	private final File srcDir;

	/**
	 * Constructor.
	 * 
	 * @param srcDir
	 *            The source dir
	 * @param log
	 *            Maven log
	 * @param project
	 *            Maven project
	 * @param helper
	 *            UnArchiver helper
	 * @param sourceEncoding
	 *            Source encoding
	 * @param filter
	 *            Source files filter
	 * @param filterParameters
	 *            Filter parameters
	 */
	public SrcDirURIResolver(File srcDir, Log log, MavenProject project,
			UnArchiverHelper helper, String sourceEncoding, String filter,
			Map filterParameters) {
		super(log, project, helper, sourceEncoding, filter, filterParameters);
		this.srcDir = srcDir;
	}

	public File getSrcDir() {
		return srcDir;
	}

	public Source resolve(String href, String base)
			throws TransformerConfigurationException {
		if (getLog().isDebugEnabled())
			getLog().debug("Resolving: " + href + " at srcDir: " + getSrcDir());
		File result = new File(getSrcDir(), href);
		if (exists(result))
			return createSource(result);
		return super.resolve(href, base);
	}

}
