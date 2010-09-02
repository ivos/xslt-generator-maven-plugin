package net.sf.seaf.mojo.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * URIResolver that resolves either from the received base URI, as an absolute
 * path, from project basedir or from classpath.
 * <p>
 * The resolution is performed in the above specified order.
 * <p>
 * See {@link FileResolver} for description of the absolute path, basedir and
 * classpath resolution.
 */
public class DefaultURIResolver extends FileResolver implements
		URIResolver {

	/**
	 * Constructor.
	 * 
	 * @param log
	 *            Maven log
	 * @param project
	 *            Maven project
	 * @param helper
	 *            UnArchiver helper
	 */
	public DefaultURIResolver(Log log, MavenProject project,
			UnArchiverHelper helper) {
		super(log, project, helper);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.xml.transform.URIResolver#resolve(java.lang.String,
	 * java.lang.String)
	 */
	public Source resolve(String href, String base) {
		getLog().debug("Resolving: " + href + " at base: " + base);
		// first try to resolve href from received base
		File result = null;
		try {
			File baseFile = new File(new URI(base));
			result = new File(baseFile.getParentFile(), href);
		} catch (URISyntaxException e) {
			getLog().warn("Unable to parse URI: " + base);
		}
		if (!exists(result))
			// then try to resolve otherwise
			result = resolve(href);
		if (result.exists()) {
			return new StreamSource(result);
		}
		return null;
	}

}
