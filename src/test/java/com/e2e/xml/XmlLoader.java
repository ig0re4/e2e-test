package com.e2e.xml;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.e2e.exceptions.TerminableConfigurationException;

/**
 * Representation of the hierarchical configuration tree, based on the xml
 * files. The functionality performs build one configuration tree from the
 * several config files and supports inheritance and overloading of the entries.
 * <TODO> Needs to implement listener functionality in order to notify on the
 * run time about the configuration changes.
 * 
 * @author igor.s
 * 
 */
@SuppressWarnings("unused")
public class XmlLoader extends XmlContainer {
	private static final String CONFIG_PATH_ARG = "configPath";
	public static final String EXT_XML = "-config.xml";
	public static final String MAIN_CONF_FILE_NAME = "fullcycle-config.xml";
	public static final String MAIN_CONF_FILE_SCHEMA_NAME = "fullcycle-config.xsd";
	private static File mainFileSchema;
	private static File mainFile;
	private String configPath = "";

	// Singleton instance
	private static XmlLoader instance = null;

	private static Logger logger = LoggerFactory.getLogger(XmlLoader.class);

	/**
	 * @throws Exception
	 * @returns instance of the singleton
	 */
	public static XmlLoader getInstance()
			throws TerminableConfigurationException {
		if (null == instance) {
			instance = createInstance();

		}
		return instance;
	}

	/**
	 * Creates instance on demand
	 * 
	 * @return ConfigurationLoader instance
	 * @throws TerminableConfigurationException
	 * @throws Exception
	 */
	private static XmlLoader createInstance()
			throws TerminableConfigurationException {
		synchronized (XmlLoader.class) {
			if (instance == null) {
				try {
					instance = new XmlLoader();
					// TODO: Add XML config schema (If not in DB...)
					// TODO: Add configuration validation
				} catch (Exception e) {
					throw new TerminableConfigurationException(
							"Failed to load configuration", e);
				}
			}
			return instance;
		}
	}

	/**
	 * Constructor builds the configuration tree.
	 * 
	 * @throws Exception
	 */
	private XmlLoader() throws Exception {
	}

	/**
	 * Initialization of the tree model
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * 
	 * @return SegmentDealCode
	 */
	public void init(String configPath) throws ParserConfigurationException,
			SAXException, IOException, Exception {
		this.configPath = configPath;
		Set<String> filePathList = new LinkedHashSet<String>();
		// generate the global xml file path
		filePathList.add(new File(this.configPath + File.separator
				+ MAIN_CONF_FILE_NAME).getCanonicalPath());

		File folder = new File(configPath);
		File[] matchingFiles = folder.listFiles(new ConfigFileFilter("",
				EXT_XML));

		for (File file : matchingFiles) {
			filePathList.add(file.getCanonicalPath());
		}

		updateTree(filePathList);
	}

	/**
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws Exception
	 */
	public void init() throws ParserConfigurationException, SAXException,
			IOException, Exception {
		root = null;
		init(getSysEnvParameter(CONFIG_PATH_ARG));
	}

	/**
	 * @param argumentName
	 * @param sysEnvName
	 * @return parameter value from the system environment or from the vm
	 *         argument
	 * @throws IOException
	 * @throws Exception
	 */
	public String getSysEnvParameter(String argumentName, String sysEnvName)
			throws IOException {
		Map<String, String> sysEnvironmentMap = System.getenv();
		String parameter = System.getProperty(argumentName);
		if (parameter == null) {
			parameter = sysEnvironmentMap.get(sysEnvName);
		}
		// check if parameter found
		if (parameter == null) {
			throw new IOException("The parameter not found for argument="
					+ argumentName + " system environment name=" + sysEnvName
					+ ", please verify !!!");
		}
		return parameter;
	}

	/**
	 * @return parameter value from the system environment or from the vm
	 *         argument
	 * @throws IOException
	 * @throws Exception
	 */
	public String getSysEnvParameter(String argumentName) throws IOException {
		return getSysEnvParameter(argumentName, argumentName);
	}

	@Override
	public String toString() {
		String result = "NO DATA";
		if (root != null) {
			result = root.toString();
		}
		return result;
	}

	public class ConfigFileFilter implements FileFilter {
		String extension;
		String prefix;

		public ConfigFileFilter(String prefix, String extension) {
			this.extension = extension;
			this.prefix = prefix;
		}

		public boolean accept(File pathname) {
			String fileName = pathname.getName();
			return fileName.startsWith(prefix) && fileName.endsWith(extension)
					&& (fileName.indexOf("log4j") == -1);
		}
	}

	public static File getMainFile() {
		return mainFile;
	}

	public static File getMainConfFileSchema() {
		return XmlLoader.mainFileSchema;
	}

	/**
	 * @return the configPath
	 */
	public String getConfigPath() {
		return configPath;
	}
}
