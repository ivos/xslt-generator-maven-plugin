package net.sf.xsltmp.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import net.sf.xsltmp.filter.Filter;

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
public class DefaultURIResolver extends FileResolver implements URIResolver {

	/**
	 * Type of filter to use. Must implement {@link Filter}.
	 * 
	 * @parameter
	 */
	private final String filterType;

	/**
	 * Map of parameters to be passed to the filter.
	 * <p>
	 * See the particular filter for its parameters.
	 * 
	 * @parameter
	 */
	private final Map filterParameters;

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
			UnArchiverHelper helper, String filter, Map filterParameters) {
		super(log, project, helper);
		this.filterType = filter;
		this.filterParameters = filterParameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.xml.transform.URIResolver#resolve(java.lang.String,
	 * java.lang.String)
	 */
	public Source resolve(String href, String base)
			throws TransformerConfigurationException {
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
			return createSource(result);
		}
		return null;
	}

	protected Source createSource(File file)
			throws TransformerConfigurationException {
		try {
			Reader reader = new FileReader(file);
			reader = wrapInFilter(reader, file.toString());
			StreamSource source = new StreamSource(reader);
			source.setSystemId(file);
			return source;
		} catch (FileNotFoundException fnfe) {
			throw new TransformerConfigurationException("File not found: "
					+ file, fnfe);
		}
	}

	private Filter filter;

	private Reader wrapInFilter(Reader reader, String name)
			throws TransformerConfigurationException {
		if (null != filterType) {
			try {
				if (null == filter) {
					getLog().debug("Initializing filter: " + filterType);
					filter = (Filter) Class.forName(filterType).newInstance();
					filter.setMavenProject(getProject());
					filter.setFileResolver(this);
					filter.setFilterParameters(filterParameters);
					filter.init();
				}
				getLog().debug("Applying filter: " + filterType);
				return filter.filter(reader, name);
			} catch (Exception e) {
				e.printStackTrace();
				throw new TransformerConfigurationException(
						"Cannot process filter: " + filterType, e);
			}
		}
		return reader;
	}

	public Source resolveAsSource(String filePath)
			throws TransformerConfigurationException {
		File file = resolve(filePath);
		return createSource(file);
	}

}
