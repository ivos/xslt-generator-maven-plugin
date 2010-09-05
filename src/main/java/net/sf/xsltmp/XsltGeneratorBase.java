package net.sf.xsltmp;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import net.sf.xsltmp.util.DefaultURIResolver;
import net.sf.xsltmp.util.UnArchiverHelper;

import org.apache.maven.plugin.AbstractMojo;
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

	public Transformer getTransformer()
			throws TransformerConfigurationException {
		if (null == transformer)
			createTransformer();
		return transformer;
	}

	public DefaultURIResolver getResolver() {
		if (null == resolver) {
			getLog().debug("Setting up DefaultURIResolver");
			resolver = new DefaultURIResolver(getLog(), getProject(),
					getHelper());
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
	 */
	private void createTransformer() throws TransformerConfigurationException {
		getLog().debug("Creating transformer...");
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setURIResolver(getResolver());
		transformer = factory.newTransformer(new StreamSource(getXslFile()));
		applyParameters();
	}

	/**
	 * Apply parameters to the transformer.
	 * 
	 * @throws TransformerConfigurationException
	 */
	private void applyParameters() throws TransformerConfigurationException {
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

	protected boolean isUpToDate(File srcFile) {
		return getTimestamp().lastModified() > srcFile.lastModified();
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
