package net.sf.xsltmp.util;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;

/**
 * Utility methods for encoding processing.
 */
public class EncodingUtils {

	/**
	 * If an encoding is not filled, default it by platform encoding.
	 * 
	 * @param encoding
	 *            Current encoding
	 * @param log
	 *            Maven log
	 * @return Encoding defaulted by platform encoding
	 */
	public String defaultByPlatformEncoding(String encoding, Log log) {
		if (StringUtils.isEmpty(encoding)) {
			encoding = System.getProperty("file.encoding");
			log.warn("Source encoding has not been set, "
					+ "using platform encoding " + encoding
					+ ", i.e. build is platform dependent!");
		}
		return encoding;
	}

}
