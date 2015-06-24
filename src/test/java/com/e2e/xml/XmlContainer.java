package com.e2e.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.e2e.exceptions.EntryNoFoundException;
import com.e2e.exceptions.TerminableConfigurationException;
import com.e2e.xml.XmlNode.XmlType;

public class XmlContainer {
	private static Logger logger = LoggerFactory.getLogger(XmlContainer.class);
	// Delimiter of the path
	public static final String DELIMETER_PATH = "/";
	// the root of the configuration tree
	protected XmlNode root = null;
	public static final String ROOT_NAME = "fullcycle";

	private ConcurrentMap<String, Set<XmlListener>> listenersMap = new ConcurrentHashMap<String, Set<XmlListener>>();
	private String rootPath = null;

	/**
	 * @param fileRoot
	 */
	private void setRoot(XmlNode fileRoot) {
		root = fileRoot;
		root.setId(root.getName());
		root.setValue(DELIMETER_PATH);
		rootPath = root.getName() + DELIMETER_PATH;
	}

	public void removeRoot() {
		root = null;
	}

	/**
	 * @return the root
	 */
	public XmlNode getRoot() {
		return root;
	}

	public StringBuffer getRootPath() {
		return new StringBuffer(rootPath);
	}

	/**
	 * update global tree from the file.
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void updateTree(Collection<String> filePathList)
			throws ParserConfigurationException, SAXException, IOException {

		for (String filePath : filePathList) {

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document document = docBuilder.parse(new File(filePath));

			// receives the root of the xml file
			XmlNode fileRoot = readXml(document.getDocumentElement(), filePath);
			// get location of the root in the global tree
			XmlNode parent = fileRoot.getParentNode();
			if (parent != null) {
				parent.setInvisible();
				// check if the root
				if (parent.isRoot()) {
					setRoot(fileRoot);
				} else {
					// if its not root integrate the root of the file in the
					// global tree
					XmlNode sourceNode = getEntry(parent.getValue());
					if (sourceNode != null) {
						sourceNode.addSon(fileRoot);
					} else {
						throw new SAXException(
								"The root location of the "
										+ filePath
										+ " is misconfigured. The global tree missing the "
										+ parent.getValue()
										+ ". Please verify !!!");
					}
				}
				updateDefaults(root);
			} else {
				throw new SAXException("The root of the " + filePath
						+ " missing location entry !!!");
			}
		}
		updateId(root);
	}

	/**
	 * update global tree from the file.
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void updateTree(InputStream is, String filePath)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document document = docBuilder.parse(is);
		// receives the root of the xml file
		XmlNode fileRoot = readXml(document.getDocumentElement(), filePath);
		// get location of the root in the global tree
		XmlNode parent = fileRoot.getParentNode();
		if (parent != null) {
			parent.setInvisible();
			// check if the root
			if (parent.isRoot()) {
				setRoot(fileRoot);
			} else {
				// if its not root integrate the root of the file in the
				// global tree
				XmlNode sourceNode = getEntry(parent.getValue());
				if (sourceNode != null) {
					sourceNode.addSon(fileRoot);
				} else {
					throw new SAXException(
							"The root location is misconfigured. "
									+ "The global tree missing the "
									+ parent.getValue() + ". Please verify !!!");
				}
			}
			updateDefaults(fileRoot);
		} else {
			throw new SAXException("The root is missing location entry !!!");
		}
		updateId(root);
	}

	private void updateId(XmlNode node) {
		XmlNode parent = node.getParent();
		if (parent == null) {
			node.setId(node.getName());
		} else {
			if (parent.getId() != null) {
				node.setId(parent.getId() + DELIMETER_PATH
						+ node.getName());
			} else {
				node.setId(ROOT_NAME + DELIMETER_PATH + node.getName());
			}
		}
		if (node.hasSons()) {
			for (XmlNode son : node.getSons()) {
				updateId(son);
			}
		}
	}
	
	/**
	 * Reads the xml file from the specific node.
	 * 
	 * @param node
	 * @param filePath
	 * @return
	 */
	private XmlNode readXml(Node node, String filePath) {
		// node name is the entry name
		String name = node.getNodeName();
		String value = null;
		// check if the value exist and read it
		// example - <a>b</a> - "a" the entry name and "b" is a value
		Node nodeValue = node.getFirstChild();
		if ((nodeValue != null) && (nodeValue.getNodeType() == Node.TEXT_NODE)
				&& (nodeValue.getTextContent().trim().length() > 0)) {
			value = nodeValue.getTextContent().trim();
		} else if (nodeValue instanceof CharacterData) {
			CharacterData cData = (CharacterData) nodeValue;
			String data = cData.getData();
			if ((data != null) && (data.trim().length() > 0)) {
				value = data.trim();
			}
		}
		// create config node instance
		XmlType valueType = XmlType.STRING;
		Node type = node.getAttributes().getNamedItem("type");
		if(type != null){
			valueType = XmlType.valueOf(type.getNodeValue());
		}
		
		XmlNode configNode = new XmlNode(name, value, filePath, valueType);
		// add the node sons
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				configNode.addSon(readXml(currentNode, filePath));
			}
		}
		return configNode;
	}
	
	/**
	 * Updates the default tree in case if we have a "default" entry which
	 * pointed on another entry. The update is perform on the specific node
	 * level and down. example: <b> <b1 /> <b2 /> </b> <a> <default>b</defailt>
	 * </a> In the tree the a will get all entries located in the <b>
	 * 
	 * @param node
	 *            the start node
	 */
	private void updateDefaults(XmlNode node) {
		Collection<XmlNode> sons = node.getSortedSons();
		if (sons != null) {
			Collection<XmlNode> cachedSons = new ArrayList<XmlNode>(sons.size());
			cachedSons.addAll(sons);
			for (XmlNode son : cachedSons) {
				// check if the son is the "default"
				if (son.getName().equals(XmlNode.DEFAULT_ENTRY)) {
					son.setInvisible();
					// get the root of the "default" entry
					XmlNode rootDefaultNode = getEntry(son.getValue());
					if (rootDefaultNode != null) {
						try {
							updateDefaults(rootDefaultNode);
							// merge the root default tree with the current node
							// tree.
							merge((XmlNode) rootDefaultNode.clone(), node);
						} catch (Exception e) {
							logger.error("Exception occured", e);
						}
					}
				}
				// go down on tree and update the defaults
				updateDefaults(son);
			}
		}
	}

	/**
	 * Merge of the two trees from the srcNode to the destNode. Actually its
	 * simple copy from source to destination. The copy will manage only if the
	 * node is not exist in the tree.
	 * 
	 * @param srcNode
	 * @param destNode
	 */
	private void merge(XmlNode srcNode, XmlNode destNode) {
		Collection<XmlNode> srcSons = srcNode.getSons();
		if (srcSons != null) {
			for (XmlNode srcSon : srcSons) {
				// check if the source son is exist in the destinaation tree
				XmlNode destSon = destNode.getSon(srcSon.getName());
				if (destSon == null) {
					// if not add it
					srcSon.setInherited(true);
					destNode.addSon(srcSon);
				} else {
					// if it does marge the subtree
					merge(srcSon, destSon);
				}
			}
		}

	}

	/**
	 * add new son to the node, update entire tree, and update the xml file
	 * 
	 * @param path
	 * @param name
	 * @param value
	 */
	public void addEntrySon(String path, String name, String value, XmlType valueType)
			throws DOMException, EntryNoFoundException,
			ParserConfigurationException, TransformerException, SAXException,
			IOException {
		XmlNode parent = getEntry(path);
		if (parent != null) {
			XmlNode son = new XmlNode(name, value, parent.getFilePath(), valueType);
			parent.addSon(son);
			updateDefaults(parent);
			flashXml(XmlOperation.ADD, parent, son);
		} else {
			throw new EntryNoFoundException(path);
		}
	}

	/**
	 * add new son to the node, update entire tree, and update the xml file
	 * 
	 * @param sonPath
	 * @param value
	 * @param valueType
	 */
	public boolean addEntry(String sonPath, String value, XmlType valueType) throws DOMException,
			ParserConfigurationException, TransformerException, SAXException,
			IOException, EntryNoFoundException {
		boolean isSucceed = false;
		if (sonPath.endsWith(DELIMETER_PATH)) {
			sonPath = sonPath.substring(0, sonPath.length() - 1);
		}
		int endIndex = sonPath.lastIndexOf(DELIMETER_PATH);
		String path = sonPath.substring(0, endIndex);
		String name = sonPath.substring(endIndex + 1);
		XmlNode parent = getEntry(path);
		if (parent != null) {
			XmlNode son = new XmlNode(name, value, parent.getFilePath(), valueType);
			son.setId(sonPath);
			parent.addSon(son);
			updateDefaults(parent);
			flashXml(XmlOperation.ADD, parent, son);
			isSucceed = true;
		} else {
			throw new EntryNoFoundException(path);
		}
		return isSucceed;
	}

	/**
	 * update son of the node, update entire tree, and update the xml file
	 * 
	 * @param path
	 * @param name
	 * @param value
	 */
	public void updateEntrySon(String path, String name, String value, XmlType valueType)
			throws DOMException, ParserConfigurationException,
			TransformerException, SAXException, IOException {
		XmlNode parent = getEntry(path);
		if (parent != null) {
			if(parent.isInherited()){
				updateEntrySon(parent.getId(), parent.getName(), parent.getValue(), parent.getType());
			}
			XmlNode son = new XmlNode(name, value, parent.getFilePath(), valueType);
			parent.addSon(son);
			updateDefaults(parent);
			flashXml(XmlOperation.UPDATE, parent, son);
		} else {
			logger.error("can't find node for path=" + path
					+ ", please verify !!!");
		}
	}

	/**
	 * update son of the node, update entire tree, and update the xml file
	 * 
	 * @param path
	 * @param value
	 */
	public boolean updateEntry(String path, String value) throws DOMException,
			ParserConfigurationException, TransformerException, SAXException,
			IOException {
		boolean isSucceed = false;
		XmlNode son = getEntry(path);
		if (son != null) {
			son.setValue(value);
			updateDefaults(son.getParent());
			flashXml(XmlOperation.UPDATE, son.getParent(), son);
			isSucceed = true;
		} else {
			logger.error("can't find node for path=" + path
					+ ", please verify !!!");
		}
		return isSucceed;
	}

	/**
	 * remove son of the node and update the xml file
	 * 
	 * @param path
	 * @param name
	 */
	public void removeEntrySon(String path, String name) throws DOMException,
			ParserConfigurationException, TransformerException, SAXException,
			IOException {
		XmlNode parent = getEntry(path);
		if (parent != null) {
			XmlNode son = parent.removeSon(name);
			updateDefaults(parent);
			flashXml(XmlOperation.REMOVE, parent, son);
		} else {
			logger.error("can't find node for path=" + path
					+ ", please verify !!!");
		}
	}

	/**
	 * remove son of the node and update the xml file
	 * 
	 * @param path
	 * @throws IOException
	 * @throws SAXException
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 */
	public boolean removeEntry(String path) throws DOMException,
			ParserConfigurationException, TransformerException, SAXException,
			IOException {
		boolean isSucceed = false;
		XmlNode son = getEntry(path);
		if (son != null) {
			XmlNode parent = son.getParent();
			if (parent != null) {
				parent.removeSon(son.getName());
				updateDefaults(parent);
				flashXml(XmlOperation.REMOVE, parent, son);
				isSucceed = true;
			}
		} else {
			logger.error("can't find node for path=" + path
					+ ", please verify !!!");
		}
		return isSucceed;
	}

	/**
	 * @param operation
	 * @param parent
	 * @param son
	 * @return
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws SAXException
	 * @throws IOException
	 */
	private boolean flashXml(XmlOperation operation, XmlNode parent, XmlNode son)
			throws DOMException, ParserConfigurationException,
			TransformerException, SAXException, IOException {
		String filepath = parent.getFilePath();
		boolean isSucceed = false;
		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(filepath);

			switch (operation) {
			case ADD:
				addNodeXml(parent, son, doc);
				break;
			case REMOVE:
				removeNodeXml(parent, son, doc);
				break;
			case UPDATE:
				updateNodeXml(parent, son, doc);
				break;
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(filepath));
			transformer.transform(source, result);

			isSucceed = true;
			notifyListeners(son.getId());
		} catch (ParserConfigurationException pce) {
			logger.error("Exception occured", pce);
			throw pce;
		} catch (TransformerException tfe) {
			logger.error("Exception occured", tfe);
			throw tfe;
		} catch (IOException ioe) {
			logger.error("Exception occured", ioe);
			throw ioe;
		} catch (SAXException sae) {
			logger.error("Exception occured", sae);
			throw sae;
		}

		return isSucceed;
	}

	/**
	 * 
	 * @param son
	 * @param doc
	 * @param parent
	 */
	private void addNodeXml(XmlNode parent, XmlNode son, Document doc)
			throws DOMException {
		// Get the parentNode element by tag name directly
		Node parentNode = doc.getElementsByTagName(parent.getName()).item(0);
		// append a new node to parentNode
		Element sonNode = doc.createElement(son.getName());
		sonNode.appendChild(doc.createTextNode(son.getValue()));
		parentNode.appendChild(sonNode);
	}

	/**
	 * 
	 * @param son
	 * @param doc
	 * @param parent
	 */
	private void removeNodeXml(XmlNode parent, XmlNode son, Document doc) {
		// Get the parentNode element by tag name directly
		Node parentNode = doc.getElementsByTagName(parent.getName()).item(0);
		Node sonNode = doc.getElementsByTagName(son.getName()).item(0);
		parentNode.removeChild(sonNode);
	}

	/**
	 * 
	 * @param son
	 * @param doc
	 * @param parent
	 */
	private void updateNodeXml(XmlNode parent, XmlNode son, Document doc) {
		// Get the parentNode element by tag name directly
		Node parentNode = doc.getElementsByTagName(parent.getName()).item(0);
		Node oldSonNode = null;
		NodeList nodeList = parentNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeName().equals(son.getName())) {
				oldSonNode = node;
				break;
			}
		}
		// append a new node to parentNode
		Element newSonNode = doc.createElement(son.getName());
		newSonNode.appendChild(doc.createTextNode(son.getValue()));

		parentNode.replaceChild(newSonNode, oldSonNode);
	}

	/**
	 * Prints the configuration tree from the specific node
	 * 
	 * @param node
	 */
	public void printTree(XmlNode node) {
		logger.info(node.toString());
		Collection<XmlNode> sons = node.getSons();
		if (sons != null) {
			for (XmlNode son : sons) {
				printTree(son);
			}
		}
	}

	/**
	 * 
	 * @param builder
	 * @param filter
	 */
	public void dumpConfig(StringBuilder builder, String filter) {
		printTreeAsString(builder, root, 0, filter);
	}

	/**
	 * 
	 * @param builder
	 * @param node
	 * @param tabsCount
	 * @param filter
	 */
	private void printTreeAsString(StringBuilder builder, XmlNode node,
			int tabsCount, String filter) {
		if (!node.getName().equals(filter) && node.isVisible()) {
			builder.append("\n\r");
			for (int index = 0; index < tabsCount; index++) {
				builder.append("    ");
			}
			builder.append(node.toBriefString());
			Collection<XmlNode> sons = node.getSons();
			if (sons != null) {
				for (XmlNode son : sons) {
					printTreeAsString(builder, son, ++tabsCount, filter);
					tabsCount--;
				}
			}
		}
	}

	/**
	 * Receives all sons located under the entry.
	 * 
	 * @param path
	 *            to the entry
	 * @return collection of the sons or null if entry not exist
	 */
	public Collection<XmlNode> getEntrySons(String path) {
		List<XmlNode> sons = null;
		// get the entry according to path
		XmlNode node = getEntry(path);
		if (node != null && node.hasSons()) {
			sons = new ArrayList<XmlNode>();
			sons.addAll(node.getSonsExceptDefault());
			Collections.sort(sons);
		}
		return sons;
	}

	/**
	 * Retrieves all sons located under the entry
	 * 
	 * @param path
	 *            to the entry
	 * @return not empty collection of the sons or throws exception
	 *         if entry not exist
	 */
	public Collection<XmlNode> getMandatoryEntrySons(String path)
			throws TerminableConfigurationException {
		List<XmlNode> sons = null;
		// get the entry according to path
		XmlNode node = getMandatoryEntry(path);
		if (node != null) {
			sons = new ArrayList<XmlNode>();
			sons.addAll(node.getSonsExceptDefault());
			Collections.sort(sons);
		}
		return sons;
	}

	/**
	 * @param path
	 *            to the specific entry
	 * @return the config node according to received path
	 */
	public XmlNode getEntry(String path) {
		XmlNode node = root;
		path = path.trim();
		// first need to check if the root is exist
		if (root != null) {
			boolean isFirst = true;
			// broke path on the entries
			if (path.endsWith(DELIMETER_PATH)) {
				path = path.substring(0, path.length() - 1);
			}
			StringTokenizer st = new StringTokenizer(path, DELIMETER_PATH);
			while (st.hasMoreTokens() && (node != null)) {
				if (isFirst) {
					isFirst = false;
					// first time check the entry name of the root
					if (!node.getName().equals(st.nextToken())) {
						node = null;
					}
				} else {
					// receive son according to entry name
					node = node.getSon(st.nextToken());
				}
			}
		} else {
			logger.error("Configuration not loaded when asked for entry: "
					+ path + " (root null)");
		}
		return node;
	}

	/**
	 * @param path
	 *            to the specific entry
	 * @return the config node according to received path
	 */
	public XmlNode getEntry(StringBuffer path) {
		return getEntry(path.toString());
	}

	/**
	 * Same class as getEntry but it validated the entry exists. If not it
	 * throws MdsTerminableConfigurationException causing the application to
	 * shutdown
	 * 
	 * @param path
	 * @return
	 * @throws TerminableConfigurationException
	 */
	public XmlNode getMandatoryEntry(String path)
			throws TerminableConfigurationException {
		XmlNode entry = getEntry(path);
		if (null == entry) {
			StackTraceElement[] stackTraceElements = Thread.currentThread()
					.getStackTrace();

			if (logger.isDebugEnabled()) {
				logger.debug("Configuration method "
						+ stackTraceElements[2].getClassName() + '.'
						+ stackTraceElements[2].getMethodName() + " failed");
			}

			throw new TerminableConfigurationException(
					"Mandatory configuration entry is missing: " + path);
		} else {
			return entry;
		}

	}

	/**
	 * Retreive specific son of the entry
	 * 
	 * @param path
	 * @param sonName
	 */
	public XmlNode getEntrySon(String path, String sonName) {
		XmlNode node = null;
		Collection<XmlNode> collection = getEntrySons(path);
		if (collection != null) {
			for (XmlNode configNode : collection) {
				if (configNode.getName().equals(sonName)) {
					node = configNode;
					break;
				}
			}
		}
		return node;
	}

	/**
	 * @param path
	 *            to the specific entry
	 * @return the sons of the entry as a Properties instance
	 */
	public Properties getSonsAsProperties(String path) {
		Properties properties = new Properties();
		for (XmlNode son : getEntrySons(path)) {
			String value = son.getValue();
			if (value == null) {
				value = "";
			}
			properties.put(son.getName(), value);
		}
		return properties;
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public Map<String, String> getSonsAsMap(String path) {
		Map<String, String> map = new HashMap<String, String>();
		for (XmlNode son : getEntrySons(path)) {
			String value = son.getValue();
			if (value == null) {
				value = "";
			}
			map.put(son.getName(), value);
		}
		return map;
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public List<String> getSonsValueAsList(String path) {
		List<String> list = new ArrayList<String>();
		for (XmlNode son : getEntrySons(path)) {
			String value = son.getValue();
			if (value == null) {
				value = "";
			}
			list.add(value);
		}
		return list;
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public List<String> getSonNamesAsList(String path) {
		List<String> list = new ArrayList<String>();
		for (XmlNode son : getEntrySons(path)) {
			String name = son.getName();
			if (name == null) {
				name = "";
			}
			list.add(name);
		}
		return list;
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public List<XmlNode> getSortedSons(String path) {
		XmlNode node = getEntry(path);
		List<XmlNode> list = null;
		if (node != null) {
			list = node.getSortedSons();
		}
		return list;
	}

	/**
	 * @return the listenersQueue
	 */
	public void addListener(String configPath, XmlListener listener) {
		Set<XmlListener> listeners = listenersMap.get(configPath);
		if (listeners == null) {
			listeners = new HashSet<XmlListener>();
			listenersMap.putIfAbsent(configPath, listeners);
		}
		listeners.add(listener);
	}

	/**
	 * @return the listenersQueue
	 */
	public void addListener(StringBuffer configPath, XmlListener listener) {
		addListener(configPath.toString(), listener);
	}

	/**
	 * @param listener
	 *            the listenersQueue to set
	 */
	public void removeListener(String configPath, XmlListener listener) {
		Set<XmlListener> listeners = listenersMap.get(configPath);
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	/**
	 * @param configPath
	 *            the listenersQueue to set
	 */
	public Set<XmlListener> getListeners(String configPath) {
		Set<XmlListener> listeners = new HashSet<XmlListener>();
		getListeners(configPath, listeners);
		if (configPath.lastIndexOf(DELIMETER_PATH) != configPath.length() - 1) {
			getListeners(configPath + DELIMETER_PATH, listeners);
		} else {
			getListeners(configPath.substring(0, configPath.length() - 1),
					listeners);
		}
		return listeners;
	}

	/**
	 * @param configPath
	 * @param listeners
	 */
	private void getListeners(String configPath, Set<XmlListener> listeners) {
		Set<XmlListener> listenerSet = listenersMap.get(configPath);
		if (listenerSet != null) {
			listeners.addAll(listenerSet);
		}
	}

	/**
	 * @param configPath
	 */
	private void notifyListeners(String configPath) {
		Set<XmlListener> listeners = getConfigListeners(configPath);
		for (XmlListener listener : listeners) {
			listener.notifyConfigChanged();
		}
	}

	/**
	 * 
	 * @param configPath
	 * @return
	 */
	private Set<XmlListener> getConfigListeners(String configPath) {
		Set<XmlListener> listeners = new HashSet<XmlListener>();
		listeners.addAll(getListeners(configPath));
		int index = configPath.lastIndexOf(DELIMETER_PATH);
		if (index != -1) {
			listeners
					.addAll(getConfigListeners(configPath.substring(0, index)));
		}
		return listeners;
	}

	/**
	 * 
	 * @author igor.s
	 * 
	 */
	public enum XmlOperation {
		ADD(0, "add xml operation"), REMOVE(1, "remove xml operation"), UPDATE(
				2, "delete xml operation");

		private int code = -1;
		private String description = null;

		XmlOperation(int code, String description) {
			this.setCode(code);
			this.setDescription(description);
		}

		/**
		 * @return the code
		 */
		public int getCode() {
			return code;
		}

		/**
		 * @param code
		 *            the code to set
		 */
		public void setCode(int code) {
			this.code = code;
		}

		/**
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * @param description
		 *            the description to set
		 */
		public void setDescription(String description) {
			this.description = description;
		}

		@Override
		public String toString() {
			return description;
		}
	}
}
