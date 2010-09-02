package net.sf.seaf.mojo.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;

/**
 * Gets files from archives.
 * <p>
 * Maintains a repository of all extracted files in a given directory. The
 * repository is organized by the archive name and the path of the files within
 * the archives.
 * <p>
 * When a file has already been extracted previously, it is served from the
 * repository and not extracted again.
 * <p>
 * When it is not present in the repository, extracts it from the archive to the
 * repository based on the requested file path within the archive and then
 * serves it from the repository.
 * <p>
 * Once a file has been extracted from a given archive, remembers the path of
 * the file within the archive. Subsequent requests for files within the same
 * archive may specify the path relative to the path of the last request.
 */
public class UnArchiverHelper {

	/**
	 * Maven log.
	 */
	private final Log log;

	/**
	 * Maven project.
	 */
	private final MavenProject project;

	/**
	 * Plexus archiver manager.
	 */
	private final ArchiverManager archiverManager;

	/**
	 * The directory of the repository of extracted files.
	 */
	private final File commonExtractDir;

	/**
	 * Map of last valid paths within archives.
	 */
	private final Map lastArchiveValidSubdir;

	/**
	 * Constructor.
	 * 
	 * @param log
	 *            Maven log
	 * @param project
	 *            Maven project
	 * @param archiverManager
	 *            Plexus archiver manager
	 * @param commonExtractDir
	 *            The directory of the repository of extracted files
	 */
	public UnArchiverHelper(Log log, MavenProject project,
			ArchiverManager archiverManager, String commonExtractDir) {
		this.log = log;
		this.project = project;
		this.archiverManager = archiverManager;
		this.commonExtractDir = new File(
				getProject().getBuild().getDirectory(), commonExtractDir);
		lastArchiveValidSubdir = new HashMap();
	}

	public Log getLog() {
		return log;
	}

	public MavenProject getProject() {
		return project;
	}

	public ArchiverManager getArchiverManager() {
		return archiverManager;
	}

	public File getCommonExtractDir() {
		return commonExtractDir;
	}

	public Map getLastArchiveValidSubdir() {
		return lastArchiveValidSubdir;
	}

	/**
	 * Get a file from an archive.
	 * 
	 * @param archive
	 *            The archive file
	 * @param filePath
	 *            Relative path to the file within the archive
	 * @return File The file from the archive
	 */
	public File getFile(File archive, String filePath) {
		getLog().debug("Getting " + filePath + " from archive " + archive);
		File extractDir = getExtractDir(archive);
		// was already extracted?
		File result = new File(extractDir, filePath);
		if (!result.exists()) {
			extract(archive, filePath);
			if (!result.exists()) {
				// extracting from received file path does not work
				// ==> try extracting from last valid path dir
				String lastValidSubdir = (String) getLastArchiveValidSubdir()
						.get(archive.getName());
				String newFilePath = new File(lastValidSubdir, filePath)
						.getPath();
				extract(archive, newFilePath);
				result = new File(extractDir, newFilePath);
			} else {
				// extracting from received file path worked
				// ==> store the valid path for future use
				String subdir = new File(filePath).getParent();
				getLastArchiveValidSubdir().put(archive.getName(), subdir);
				getLog().debug(
						"Storing last valid subdir for archive "
								+ archive.getName() + " as " + subdir);
			}
		}
		return result;
	}

	/**
	 * Extract a file from an archive.
	 * 
	 * @param archive
	 *            The archive file
	 * @param filePath
	 *            Relative path to the file to be extracted within the archive
	 */
	public void extract(File archive, String filePath) {
		File extractDir = getExtractDir(archive);
		getLog().debug(
				"Extracting " + filePath + " from " + archive + " to "
						+ extractDir);
		extractDir.mkdirs();
		try {
			UnArchiver unArchiver;
			unArchiver = getArchiverManager().getUnArchiver(archive);
			unArchiver.setSourceFile(archive);
			unArchiver.setDestDirectory(extractDir);
			IncludeExcludeFileSelector[] selectors = new IncludeExcludeFileSelector[] { new IncludeExcludeFileSelector() };
			selectors[0].setIncludes(new String[] { filePath });
			unArchiver.setFileSelectors(selectors);
			unArchiver.extract();
		} catch (NoSuchArchiverException e) {
			getLog().warn("Unknown archiver type");
		} catch (ArchiverException e) {
			getLog().warn(
					"Error unpacking file: " + archive + "\t" + e.toString());
		}
	}

	/**
	 * Return the extract dir for an archive
	 * 
	 * @param archive
	 *            The archive file
	 * @return File The directory to where files from the archive should be
	 *         extracted
	 */
	private File getExtractDir(File archive) {
		return new File(commonExtractDir, archive.getName());
	}

}
