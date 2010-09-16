package net.sf.xsltmp.filter;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.sf.xsltmp.util.BundleLoader;
import net.sf.xsltmp.util.FileResolver;
import net.sf.xsltmp.util.StreamTranslator;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Filter that performs translation of input files.
 * <p>
 * Adapted from Apache Ant translate task.
 */
public class TranslatingFilter implements Filter {

	/**
	 * Starting token to identify keys.
	 */
	private String startToken;

	/**
	 * Ending token to identify keys.
	 */
	private String endToken;

	/**
	 * Family name of resource bundle
	 */
	private String bundle;

	/**
	 * Locale specific language of the resource bundle
	 */
	private String bundleLanguage;

	/**
	 * Locale specific country of the resource bundle
	 */
	private String bundleCountry;

	/**
	 * Locale specific variant of the resource bundle
	 */
	private String bundleVariant;

	/**
	 * Resource Bundle file encoding scheme, defaults to srcEncoding
	 */
	private String bundleEncoding;

	private FileResolver fileResolver;

	private MavenProject project;

	/**
	 * Maven log.
	 */
	private Log log;

	/**
	 * Holds key value pairs loaded from resource bundle file.
	 */
	private final Map resourceMap = new HashMap();

	private Map filterParameters;

	public void setMavenProject(MavenProject project) {
		this.project = project;
	}

	public void setFileResolver(FileResolver fileResolver) {
		this.fileResolver = fileResolver;
		this.log = fileResolver.getLog();
	}

	public void setFilterParameters(Map filterParameters) {
		this.filterParameters = filterParameters;
		startToken = getParam("startToken", "#");
		endToken = getParam("endToken", "#");
		bundle = getParam("bundle", "Resource");
		bundleLanguage = getParam("bundleLanguage", Locale.getDefault()
				.getLanguage());
		bundleCountry = getParam("bundleCountry", Locale.getDefault()
				.getCountry());
		bundleVariant = getParam("bundleVariant", Locale.getDefault()
				.getVariant());
		bundleEncoding = getParam("bundleEncoding", project.getProperties()
				.get("project.build.sourceEncoding"));
		if (log.isDebugEnabled())
			log.debug("Initialized TranslatingFilter: startToken=" + startToken
					+ ", endToken=" + endToken + ", bundle=" + bundle
					+ ", bundleLanguage=" + bundleLanguage + ", bundleCountry="
					+ bundleCountry + ", bundleVariant=" + bundleVariant
					+ ", bundleEncoding=" + bundleEncoding);
	}

	private String getParam(String key, Object defaultValue) {
		if (null != filterParameters && filterParameters.containsKey(key))
			return (String) filterParameters.get(key);
		return (String) defaultValue;
	}

	public void init() {
		new BundleLoader(bundle, bundleLanguage, bundleCountry, bundleVariant,
				bundleEncoding, fileResolver, resourceMap).loadBundle();
	}

	public Reader filter(Reader reader, String name) throws IOException {
		if (log.isDebugEnabled())
			log.debug("Filtering file: " + name);
		return new StreamTranslator(startToken, endToken, resourceMap, log)
				.translate(reader);
	}

}
