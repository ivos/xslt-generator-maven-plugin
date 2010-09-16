package net.sf.xsltmp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.logging.Log;

/**
 * Loads resources from a bundle.
 */
public class BundleLoader {

	/**
	 * Family name of resource bundle
	 */
	private final String bundle;

	/**
	 * Locale specific language of the resource bundle
	 */
	private final String bundleLanguage;

	/**
	 * Locale specific country of the resource bundle
	 */
	private final String bundleCountry;

	/**
	 * Locale specific variant of the resource bundle
	 */
	private final String bundleVariant;

	/**
	 * Resource Bundle file encoding scheme, defaults to srcEncoding
	 */
	private final String bundleEncoding;

	private final FileResolver fileResolver;

	/**
	 * Maven log.
	 */
	private final Log log;

	/**
	 * Map to be loaded.
	 */
	private final Map resourceMap;

	/**
	 * Create bundle loader.
	 * 
	 * @param bundle
	 *            Bundle name
	 * @param bundleLanguage
	 *            Bundle language
	 * @param bundleCountry
	 *            Bundle country
	 * @param bundleVariant
	 *            Bundle variant
	 * @param bundleEncoding
	 *            Bundle encoding
	 * @param fileResolver
	 *            File resolver to retrieve the bundle file
	 * @param resourceMap
	 *            Target map to be loaded
	 */
	public BundleLoader(String bundle, String bundleLanguage,
			String bundleCountry, String bundleVariant, String bundleEncoding,
			FileResolver fileResolver, Map resourceMap) {
		this.bundle = bundle;
		this.bundleLanguage = bundleLanguage;
		this.bundleCountry = bundleCountry;
		this.bundleVariant = bundleVariant;
		this.bundleEncoding = bundleEncoding;
		this.fileResolver = fileResolver;
		this.log = fileResolver.getLog();
		this.resourceMap = resourceMap;
	}

	private boolean loaded = false;

	/**
	 * Load the bundle.
	 */
	public void loadBundle() {
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
			if (log.isDebugEnabled())
				log.debug("Loaded properties: " + propsFile);
		} catch (IOException e) {
			if (log.isDebugEnabled())
				log.debug("Cannot load properties: " + propsFile);
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

}
