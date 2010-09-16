package net.sf.xsltmp.filter;

import java.io.Reader;
import java.util.Map;

import net.sf.xsltmp.util.FileResolver;

import org.apache.maven.project.MavenProject;

/**
 * Filters input files (XSL templates and source files) on-the-fly before
 * applying the XSL transformation.
 */
public interface Filter {

	/**
	 * Set the filter parameters.
	 * 
	 * @param filterParameters
	 */
	void setFilterParameters(Map filterParameters);

	/**
	 * Set Maven project instance.
	 * 
	 * @param project
	 */
	void setMavenProject(MavenProject project);

	/**
	 * Set file resolver.
	 * 
	 * @param fileResolver
	 */
	void setFileResolver(FileResolver fileResolver);

	/**
	 * Perform filter initialization.
	 * <p>
	 * Called after properties are set.
	 */
	void init();

	/**
	 * Filter an input file.
	 * 
	 * @param reader
	 *            A Reader of the input file to filter
	 * @param name
	 *            Name of the input file
	 * @return A Reader of filtered input file
	 * @throws Exception
	 */
	Reader filter(Reader reader, String name) throws Exception;

}
