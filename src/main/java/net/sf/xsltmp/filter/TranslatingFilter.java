package net.sf.xsltmp.filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import net.sf.xsltmp.util.FileResolver;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.util.LineTokenizer;

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

	public void setMavenProject(MavenProject project) {
		this.project = project;
	}

	public void setFileResolver(FileResolver fileResolver) {
		this.fileResolver = fileResolver;
		this.log = fileResolver.getLog();
	}

	public void init() {
		loadResourceMaps();
	}

	private boolean loaded = false;

	private void loadResourceMaps() {
		loadResourceMaps(new Locale(bundleLanguage, bundleCountry,
				bundleVariant));
		loadResourceMaps(Locale.getDefault());
		if (!loaded)
			throw new IllegalArgumentException("Bundle cannot be loaded "
					+ bundle);
	}

	private void loadResourceMaps(Locale locale) {
		String language = locale.getLanguage().length() > 0 ? "_"
				+ locale.getLanguage() : "";
		String country = locale.getCountry().length() > 0 ? "_"
				+ locale.getCountry() : "";
		String variant = locale.getVariant().length() > 0 ? "_"
				+ locale.getVariant() : "";
		processBundle(bundle + language + country + variant);
		processBundle(bundle + language + country);
		processBundle(bundle + language);
		processBundle(bundle);
	}

	private void processBundle(String bundleFile) {
		File propsFile = fileResolver.resolve(bundleFile + ".properties");
		try {
			Properties properties = new Properties();
			properties.load(new InputStreamReader(
					new FileInputStream(propsFile), bundleEncoding));
			copyNonExistingEntries(properties);
			loaded = true;
			log.debug("Loaded properties " + propsFile);
		} catch (IOException e) {
			log.debug("Cannot load properties " + propsFile);
		}
	}

	private void copyNonExistingEntries(Properties properties) {
		for (Iterator iterator = properties.keySet().iterator(); iterator
				.hasNext();) {
			String key = (String) iterator.next();
			if (!resourceMap.containsKey(key))
				resourceMap.put(key, properties.get(key));
		}
	}

	private String line;

	public Reader filter(Reader reader, String name) throws IOException {
		log.debug("Filtering file " + name);
		StringBuilder sb = new StringBuilder();
		LineTokenizer lineTokenizer = new LineTokenizer();
		lineTokenizer.setIncludeDelims(true);
		line = lineTokenizer.getToken(reader);
		while ((line) != null) {
			String lineBefore = line;
			translateLine();
			if (log.isDebugEnabled() && !lineBefore.equals(line)) {
				log.debug("Line before: " + lineBefore.trim());
				log.debug("Line after : " + line.trim());
			}
			sb.append(line);
			line = lineTokenizer.getToken(reader);
		}
		return new StringReader(sb.toString());
	}

	private int startIndex, endIndex;

	private void translateLine() {
		startIndex = 0;
		nextStartToken();
		while (isStartIndexValid()) {
			nextEndIndex();
			if (endIndex < 0) {
				skip();
			} else {
				String token = getToken();
				if (log.isDebugEnabled())
					log.debug("Translating token " + token);
				if (!isTokenValid(token)) {
					skip();
				} else {
					replaceToken(getReplaceFor(token));
				}
			}
			nextStartToken();
		}
	}

	private boolean isStartIndexValid() {
		return startIndex >= 0
				&& (startIndex + startToken.length()) <= line.length();
	}

	private void skip() {
		startIndex += 1;
	}

	private void replaceToken(String replace) {
		line = line.substring(0, startIndex) + replace
				+ line.substring(endIndex + endToken.length());
		startIndex += replace.length();
	}

	private String getToken() {
		return line.substring(startIndex + startToken.length(), endIndex);
	}

	private void nextStartToken() {
		startIndex = line.indexOf(startToken, startIndex);
	}

	private void nextEndIndex() {
		endIndex = line.indexOf(endToken, startIndex + startToken.length());
	}

	private boolean isTokenValid(String token) {
		for (int k = 0; k < token.length(); k++) {
			char c = token.charAt(k);
			if (c == ':' || c == '=' || Character.isSpaceChar(c)) {
				return false;
			}
		}
		return true;
	}

	private String getReplaceFor(String token) {
		if (resourceMap.containsKey(token)) {
			return (String) resourceMap.get(token);
		}
		log.warn("Replacement string missing for: " + token);
		return startToken + token + endToken;
	}

}
