package net.sf.seaf.mojo.xsltgenerator;

import java.io.File;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.seaf.mojo.util.DefaultURIResolver;
import net.sf.seaf.mojo.util.UnArchiverHelper;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * URIResolver that resolves either from source directory, the received base
 * URI, as an absolute path, from project basedir or from classpath.
 * <p>
 * First tries to resolve from the source directory.
 * <p>
 * Then resolves from the other options, see
 * {@link DefaultURIResolver} for details.
 */
public class SrcDirURIResolver extends DefaultURIResolver {

	/**
	 * The source dir
	 */
	private final File srcDir;

	/**
	 * Constructor.
	 * 
	 * @param srcDir
	 *            The source dir
	 * @param log
	 *            Maven log
	 * @param project
	 *            Maven project
	 * @param helper
	 *            UnArchiver helper
	 */
	public SrcDirURIResolver(File srcDir, Log log, MavenProject project,
			UnArchiverHelper helper) {
		super(log, project, helper);
		this.srcDir = srcDir;
	}

	public File getSrcDir() {
		return srcDir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.seaf.mojo.util.BasedirAndClasspathURIResolver#resolve(java.lang
	 * .String, java.lang.String)
	 */
	public Source resolve(String href, String base) {
		getLog().debug("Resolving: " + href + " at srcDir: " + getSrcDir());
		File result = new File(getSrcDir(), href);
		if (exists(result))
			return new StreamSource(result);
		return super.resolve(href, base);
	}

}
