package net.sf.xsltmp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

/**
 * Stores processed contents of files in a file cache to prevent duplicate
 * processing.
 */
public class FileCache {

	/**
	 * Maven log.
	 */
	private final Log log;

	private final File rebaseTo;
	private final File storeAt;
	private final String encoding;

	/**
	 * Create file cache.
	 * 
	 * @param log
	 *            Maven log
	 * @param rebaseTo
	 *            Directory against which to re-base the names of files
	 * @param storeAt
	 *            Directory where to store the processed contents of the files
	 * @param encoding
	 *            Encoding to use for the cache files
	 */
	public FileCache(Log log, File rebaseTo, File storeAt, String encoding) {
		this.log = log;
		this.rebaseTo = rebaseTo;
		this.storeAt = storeAt;
		this.encoding = encoding;
	}

	/**
	 * Store the processed content of the source file in the cache.
	 * 
	 * @param contentReader
	 *            Reader of the processed content
	 * @param sourceFile
	 *            The source file (that was processed)
	 */
	public void store(Reader contentReader, File sourceFile) {
		try {
			File stored = getStored(sourceFile);
			if (null != stored) {
				if (log.isDebugEnabled())
					log.debug("Storing content in file cache at " + stored);
				stored.getParentFile().mkdirs();
				String content = IOUtil.toString(contentReader);
				FileUtils.fileWrite(stored.getPath(), encoding, content);
				contentReader.reset();
			} else {
				if (log.isDebugEnabled())
					log.debug("File cannot be rebased, content not stored "
							+ sourceFile);
			}
		} catch (IOException ioe) {
			log.error("Cannot store content for file " + sourceFile, ioe);
		}
	}

	/**
	 * Retrieve the processed content of a file.
	 * 
	 * @param sourceFile
	 *            The file whose processed content to retrieve
	 * @return A Reader for the processed content of the file
	 */
	public Reader retrieve(File sourceFile) {
		try {
			File stored = getStored(sourceFile);
			if (null != stored && stored.exists()) {
				if (log.isDebugEnabled())
					log.debug("Retrieving content in file cache at " + stored);
				return new InputStreamReader(new FileInputStream(stored),
						encoding);
			}
		} catch (IOException ioe) {
			log.error("Cannot retrieve content for file " + sourceFile, ioe);
		}
		return null;
	}

	private File getStored(File sourceFile) throws IOException {
		String rebase = rebaseTo.getCanonicalPath();
		String filePath = sourceFile.getCanonicalPath();
		if (filePath.startsWith(rebase)) {
			String relativeName = filePath.substring(rebase.length());
			File stored = new File(storeAt, relativeName);
			return stored;
		}
		return null;
	}
}
