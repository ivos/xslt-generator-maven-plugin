package net.sf.xsltmp;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import net.sf.xsltmp.filter.Filter;
import net.sf.xsltmp.util.DefaultURIResolver;
import net.sf.xsltmp.util.UnArchiverHelper;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;

/**
 * The base class for XSLT Generator. Based on Codehaus xslt-maven-plugin.
 */
public abstract class XsltGeneratorBase extends AbstractMojo implements
		XsltGeneratorConstants {

	/**
	 * To look up Archiver/UnArchiver implementations
	 * 
	 * @component
	 */
	protected ArchiverManager archiverManager;

	/**
	 * The Maven project model.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * The XSL template to use.
	 * 
	 * @parameter
	 * @required
	 */
	private String xslTemplate;

	/**
	 * A Map of parameters to be passed into the style.
	 * 
	 * @parameter
	 */
	private Map parameters;

	/**
	 * Force generation. When set, the timestamp of the last build is ignored
	 * and the XSLT generation is always performed.
	 * 
	 * @parameter expression="${xsltmp.force}" default-value="false"
	 */
	private boolean force;

	/**
	 * Type of filter to use on input files (XSL templates and source files).
	 * Must implement {@link Filter}.
	 * 
	 * @parameter
	 */
	private String filter;

	/**
	 * Map of parameters to be passed to the filter.
	 * <p>
	 * See the particular filter for its parameters.
	 * 
	 * @parameter
	 */
	private Map filterParameters;

	private Transformer transformer = null;
	private File xslFile = null;
	protected DefaultURIResolver resolver = null;
	private UnArchiverHelper helper = null;
	private File timestamp = null;

	// Standard getters and setters for the properties

	public MavenProject getProject() {
		return project;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

	public ArchiverManager getArchiverManager() {
		return archiverManager;
	}

	public void setArchiverManager(ArchiverManager archiverManager) {
		this.archiverManager = archiverManager;
	}

	public String getXslTemplate() {
		return xslTemplate;
	}

	public void setXslTemplate(String xslTemplate) {
		this.xslTemplate = xslTemplate;
	}

	public Map getParameters() {
		if (null == parameters)
			parameters = new HashMap();
		return parameters;
	}

	public void setParameters(Map parameters) {
		this.parameters = parameters;
	}

	public boolean getForce() {
		return force;
	}

	public void setForce(boolean force) {
		this.force = force;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public Map getFilterParameters() {
		return filterParameters;
	}

	public void setFilterParameters(Map filterParameters) {
		this.filterParameters = filterParameters;
	}

	public Transformer getTransformer() throws MojoFailureException {
		if (null == transformer)
			createTransformer();
		return transformer;
	}

	public DefaultURIResolver getResolver() {
		if (null == resolver) {
			getLog().debug("Setting up DefaultURIResolver");
			resolver = new DefaultURIResolver(getLog(), getProject(),
					getHelper(), filter, filterParameters);
		}
		return resolver;
	}

	public UnArchiverHelper getHelper() {
		if (null == helper) {
			helper = new UnArchiverHelper(getLog(), getProject(),
					getArchiverManager(), EXTRACTS_DIR);
		}
		return helper;
	}

	public File getXslFile() {
		if (null == xslFile) {
			xslFile = getResolver().resolve(getXslTemplate());
		}
		return xslFile;
	}

	/**
	 * The timestamp of last run of Maven build.
	 * 
	 * @return
	 */
	public File getTimestamp() {
		if (null == timestamp) {
			File timestampDir = new File(
					getProject().getBuild().getDirectory(), BASE_DIR);
			timestamp = new File(timestampDir, TIMESTAMP_FILENAME);
		}
		return timestamp;
	}

	// Helper methods for descendants

	/**
	 * Create and configure a transformer.
	 * 
	 * @throws TransformerConfigurationException
	 * @throws MojoFailureException
	 */
	private void createTransformer() throws MojoFailureException {
		getLog().debug("Creating transformer...");
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setURIResolver(getResolver());
		try {
			transformer = factory.newTransformer(getResolver().resolveAsSource(
					getXslTemplate()));
		} catch (TransformerConfigurationException tce) {
			throw new MojoFailureException("Cannot process template file: "
					+ getXslTemplate(), tce);
		}
		applyParameters();
	}

	/**
	 * Apply parameters to the transformer.
	 * 
	 * @throws TransformerConfigurationException
	 * @throws MojoFailureException
	 */
	private void applyParameters() throws MojoFailureException {
		getLog().debug("Applying parameters...");
		if (getParameters() != null) {
			Set keys = getParameters().keySet();
			for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				getLog().debug(
						"Setting parameter: key=" + key + " value="
								+ getParameters().get(key));
				getTransformer().setParameter(key, getParameters().get(key));
			}
			if (getParameters().isEmpty())
				getLog().debug("No parameters");
		}
	}

	protected boolean verifyXsltFileExist() {
		if (!getXslFile().exists()) {
			getLog().warn(
					"Generation not performed, XSL file was not found: "
							+ getXslTemplate());
			return false;
		}
		return true;
	}

	protected String getLogPrefix() {
		return getMojoName() + ": " + getXslTemplate();
	}

	/**
	 * Has the file changed from the last run of Maven build or is re-generation
	 * forced?
	 * 
	 * @param file
	 * @return true iff the file has changed from last Maven build or
	 *         re-generation is forced
	 */
	protected boolean hasChanged(File file) {
		if (getForce())
			return true;
		return file.lastModified() > getTimestamp().lastModified();
	}

	// Abstract methods to be implemented by concrete descendants

	/**
	 * Return the name of the mojo.
	 * 
	 * For logging purposes.
	 * 
	 * @return String
	 */
	protected abstract String getMojoName();

}
