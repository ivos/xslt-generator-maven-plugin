package net.sf.xsltmp;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Initializes the XSLT generator maven plugin at the start of the build.
 * <p>
 * Prepares the timestamp of the xslt generation. This mojo should be run before
 * other executions of the xslt generator. It touches a prepared timestamp file.
 * The file is then used as the timestamp of xslt generation after all the
 * executions of the xslt generator are finished.
 * 
 * @goal initialize
 * @phase initialize
 */
public class InitializeMojo extends AbstractMojo implements
		XsltGeneratorConstants {

	/**
	 * The Maven project model.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	// Standard getters and setters for the properties

	public MavenProject getProject() {
		return project;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		File dir = new File(getProject().getBuild().getDirectory(), BASE_DIR);
		File file = new File(dir, PREPARED_TIMESTAMP_FILENAME);
		dir.mkdirs();
		try {
			file.createNewFile();
			file.setLastModified(System.currentTimeMillis());
			getLog().info("Initialized.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

}
