package net.sf.xsltmp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import net.sf.xsltmp.XsltGeneratorConstants;
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
public class DefaultURIResolver extends FileResolver implements URIResolver,
		XsltGeneratorConstants {

	/**
	 * Read the source files (XSL templates and XML transformation sources)
	 * using this encoding.
	 */
	private final String sourceEncoding;

	/**
	 * Type of filter to use. Must implement {@link Filter}.
	 */
	private final String filterType;

	/**
	 * Map of parameters to be passed to the filter.
	 * <p>
	 * See the particular filter for its parameters.
	 */
	private final Map filterParameters;

	private final FileCache filteredContent;

	/**
	 * Constructor.
	 * 
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
	public DefaultURIResolver(Log log, MavenProject project,
			UnArchiverHelper helper, String sourceEncoding, String filter,
			Map filterParameters) {
		super(log, project, helper);
		this.sourceEncoding = sourceEncoding;
		this.filterType = filter;
		this.filterParameters = filterParameters;
		File buildBase = new File(getProject().getBuild().getDirectory());
		File filteredBase = new File(buildBase, FILTERED_DIR);
		filteredContent = new FileCache(log, buildBase, filteredBase,
				sourceEncoding);
	}

	public Source resolve(String href, String base)
			throws TransformerConfigurationException {
		if (getLog().isDebugEnabled())
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
			Reader reader = new InputStreamReader(new FileInputStream(file),
					sourceEncoding);
			reader = wrapInFilter(reader, file);
			StreamSource source = new StreamSource(reader);
			source.setSystemId(file);
			return source;
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			throw new TransformerConfigurationException("File not found: "
					+ file, fnfe);
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
			throw new TransformerConfigurationException(
					"Unsupported source encoding: " + sourceEncoding, uee);
		}
	}

	private Filter filter;

	private Reader wrapInFilter(Reader reader, File file)
			throws TransformerConfigurationException {
		if (null != filterType) {
			Reader cached = filteredContent.retrieve(file);
			if (null != cached)
				return cached;
			try {
				if (null == filter) {
					if (getLog().isDebugEnabled())
						getLog().debug("Initializing filter: " + filterType);
					filter = (Filter) Class.forName(filterType).newInstance();
					filter.setMavenProject(getProject());
					filter.setFileResolver(this);
					filter.setFilterParameters(filterParameters);
					filter.init();
				}
				if (getLog().isDebugEnabled())
					getLog().debug("Applying filter: " + filterType);
				reader = filter.filter(reader, file.getPath());
				filteredContent.store(reader, file);
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
