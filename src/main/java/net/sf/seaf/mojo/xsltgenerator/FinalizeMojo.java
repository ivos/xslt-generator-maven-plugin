package net.sf.seaf.mojo.xsltgenerator;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Finalizes the XSLT generator maven plugin at the end of the build.
 * <p>
 * Sets the timestamp of the xslt generation. This mojo should be run after
 * other executions of the xslt generator. It takes the prepared timestamp file
 * and sets the timestamp based on it.
 * <p>
 * By default runs in process-resources phase, if needed, customize the phase as
 * required. Generally should be run as the last execution of the generator in
 * the build (eg. if generate-test-resources phase is the last one you use the
 * generator in, finalize it in this phase).
 * 
 * @goal finalize
 * @phase process-resources
 */
public class FinalizeMojo extends AbstractMojo implements
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
		File prepared = new File(dir, PREPARED_TIMESTAMP_FILENAME);
		File file = new File(dir, TIMESTAMP_FILENAME);
		dir.mkdirs();
		try {
			file.createNewFile();
			if (prepared.exists()) {
				file.setLastModified(prepared.lastModified());
				getLog().info("Finalized.");
			} else {
				file.setLastModified(System.currentTimeMillis());
				getLog().warn(
						"Finalized by setting timestamp as system time, "
								+ "prepared timestamp not available. "
								+ "(Did you forget to configure an initialize goal?)");
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

}
