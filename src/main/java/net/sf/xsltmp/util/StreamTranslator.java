package net.sf.xsltmp.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.apache.tools.ant.util.LineTokenizer;

/**
 * Translates a Reader stream.
 */
public class StreamTranslator {

	/**
	 * Starting token to identify keys.
	 */
	private final String startToken;

	/**
	 * Ending token to identify keys.
	 */
	private final String endToken;

	/**
	 * Holds key value pairs loaded from resource bundle file.
	 */
	private final Map resourceMap;

	/**
	 * Maven log.
	 */
	private final Log log;

	/**
	 * Create stream translator.
	 * 
	 * @param startToken
	 *            Starting token to identify keys
	 * @param endToken
	 *            Ending token to identify keys
	 * @param resourceMap
	 *            Key value pairs
	 * @param log
	 *            Maven log
	 */
	public StreamTranslator(String startToken, String endToken,
			Map resourceMap, Log log) {
		this.startToken = startToken;
		this.endToken = endToken;
		this.resourceMap = resourceMap;
		this.log = log;
	}

	private String line;

	/**
	 * Translate the Reader stream.
	 * 
	 * @param reader
	 *            Input reader to be translated
	 * @return Translated Reader stream
	 * @throws IOException
	 *             On errors reading input Reader stream.
	 */
	public Reader translate(Reader reader) throws IOException {
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
					log.debug("Translating token: " + token);
				if (!isTokenValid(token)) {
					skip();
				} else {
					replaceToken(getReplaceFor(token));
				}
			}
			nextStartToken();
		}
	}

	private void nextStartToken() {
		startIndex = line.indexOf(startToken, startIndex);
	}

	private boolean isStartIndexValid() {
		return startIndex >= 0
				&& (startIndex + startToken.length()) <= line.length();
	}

	private void nextEndIndex() {
		endIndex = line.indexOf(endToken, startIndex + startToken.length());
	}

	private void skip() {
		startIndex += 1;
	}

	private String getToken() {
		return line.substring(startIndex + startToken.length(), endIndex);
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
		log.warn("Translation missing for token: " + token);
		return startToken + token + endToken;
	}

	private void replaceToken(String replace) {
		line = line.substring(0, startIndex) + replace
				+ line.substring(endIndex + endToken.length());
		startIndex += replace.length();
	}

}
