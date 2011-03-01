package net.sf.xsltmp.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Utility methods for adding directories to sources and resources.
 */
public class AddSourcesUtils {

	private final MavenProject project;
	private final Log log;

	/**
	 * Default constructor.
	 * 
	 * @param project
	 * @param log
	 */
	public AddSourcesUtils(MavenProject project, Log log) {
		this.project = project;
		this.log = log;
	}

	/**
	 * Add a directory to project sources.
	 * 
	 * @param addTo
	 *            Add to <code>sources</code> or <code>test-sources</code>.
	 * @param dir
	 *            Directory to add
	 */
	public void addSources(String addTo, File dir) {
		String path = dir.getAbsolutePath();
		if ("sources".equals(addTo)) {
			project.addCompileSourceRoot(path);
			log.info("Added to " + addTo + " destDir: " + path);
		}
		if ("test-sources".equals(addTo)) {
			project.addTestCompileSourceRoot(path);
			log.info("Added to " + addTo + " destDir: " + path);
		}
	}

	/**
	 * Add a directory to project resources.
	 * 
	 * @param addTo
	 *            Add to <code>resources</code> or <code>test-resources</code>.
	 * @param dir
	 *            Directory to add
	 */
	public void addResources(String addTo, File dir) {
		String path = dir.getAbsolutePath();
		if ("resources".equals(addTo)) {
			project.addResource(getDirResource(dir));
			log.info("Added to " + addTo + " destDir: " + path);
		}
		if ("test-resources".equals(addTo)) {
			project.addTestResource(getDirResource(dir));
			log.info("Added to " + addTo + " destDir: " + path);
		}
	}

	/**
	 * Wrap directory into Maven resource.
	 * 
	 * @param dir
	 *            Directory to wrap
	 * @return Maven resource
	 */
	private Resource getDirResource(File dir) {
		Resource resource = new Resource();
		resource.setDirectory(dir.getAbsolutePath());
		List excludes = new ArrayList();
		excludes.add("**/*.java");
		resource.setExcludes(excludes);
		return resource;
	}

}
