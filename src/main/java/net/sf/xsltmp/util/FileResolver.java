package net.sf.xsltmp.util;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Resolves a File corresponding to a given path either as absolute path, from
 * project basedir or from classpath.
 * <p>
 * First tries to resolve the path as absolute path.
 * <p>
 * Then tries to relate the path to project basedir.
 * <p>
 * Then iterates over all compile classpath elements and tries to relate the
 * path to each one of them. If the classpath element is not a directory, but an
 * archive file, tries to extract the requested file from the archive. See
 * {@link UnArchiverHelper} for details on how extract is performed.
 */
public class FileResolver {

	/**
	 * Maven log.
	 */
	private final Log log;

	/**
	 * Maven project.
	 */
	private final MavenProject project;

	/**
	 * UnArchiver helper.
	 */
	private final UnArchiverHelper helper;

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
	public FileResolver(Log log, MavenProject project, UnArchiverHelper helper) {
		this.log = log;
		this.project = project;
		this.helper = helper;
	}

	public Log getLog() {
		return log;
	}

	public MavenProject getProject() {
		return project;
	}

	public UnArchiverHelper getHelper() {
		return helper;
	}

	/**
	 * Get a file path as a File.
	 * 
	 * @param filePath
	 *            The relative path of the file
	 * @return File The File corresponding to the path
	 */
	public File resolve(String filePath) {
		getLog().debug(
				"Resolving: " + filePath + " as absolute, "
						+ "basedir or classpath");
		// first try to resolve as absolute path
		File result = new File(filePath);
		if (exists(result))
			return result;
		// then check in project basedir
		result = new File(getProject().getBasedir(), filePath);
		if (exists(result))
			return result;
		// then in classpath
		try {
			List classpaths = getProject().getCompileClasspathElements();
			for (Iterator iterator = classpaths.iterator(); iterator.hasNext();) {
				File classpath = new File((String) iterator.next());
				if (classpath.isFile()) {
					// get from dependency archive file
					result = getHelper().getFile(classpath, filePath);
				} else {
					// get from classpath directory
					result = new File(classpath, filePath);
				}
				if (exists(result))
					return result;
			}
		} catch (DependencyResolutionRequiredException e) {
			getLog().warn(
					"Dependencies must be resolved first, could not locate "
							+ filePath + " from classpath");
		}
		getLog().debug("- Not resolved");
		return result;
	}

	/**
	 * Check existence of a file. Returns true iff the file exists.
	 * 
	 * @param file
	 *            The File to check
	 * @return boolean
	 */
	protected boolean exists(File file) {
		if (null == file)
			return false;
		getLog().debug("- Trying: " + file.getAbsolutePath());
		boolean result = file.exists();
		if (result)
			getLog().debug("- Resolved");
		return result;
	}

}
