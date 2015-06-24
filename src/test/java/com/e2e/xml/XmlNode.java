package com.e2e.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.e2e.exceptions.TerminableConfigurationException;

/**
 * @author igor.s
 */
public class XmlNode implements Comparable<XmlNode>, Cloneable{
	public enum XmlType{
		BOOLEAN, STRING, INT, NULL, ARRAY, DOUBLE;
	}
	//name of the entry, which value is points on the "parent path" 
	//"inherited" from the other entry 
	public static final String DEFAULT_ENTRY = "default";
	public static final String ROOT_PATH = "/";
	public static final String PARENT_PATH = "parent-path";
	
	private Map<String, XmlNode> sons;
	private String name;
	private String value;
	private boolean isInherited = false;
	private boolean isVisible = true;
	//instance of the parent node
	private XmlNode parent = null;
	private String id;
	private XmlType type = XmlType.STRING;
	
	private String filePath = null;
	
	private static Logger logger = LoggerFactory.getLogger(XmlNode.class);
	/**
	 * 
	 * @param name
	 * @param value
	 */
	public XmlNode(String name, String value, String filePath, XmlType type) {
		this.name = name;
		this.value = value;
		this.type = type;
		this.setFilePath(filePath);
	}

	/**
	 * 
	 * @param name
	 * @param value
	 */
	public XmlNode(String name, String value, 
			String filePath, XmlType type, boolean isInherited) {
		this(name, value, filePath, type);
		this.isInherited = isInherited;
	}

	
	/**
	 * 
	 * @param nodeName
	 * @return
	 */
	public XmlNode getSon(String nodeName) {
		XmlNode son = null;
		if(sons != null){
			son = sons.get(nodeName);
		}
		return son;
	}

    public XmlNode getMandatorySon(String nodeName)
            throws TerminableConfigurationException {
        if((sons == null) || !sons.containsKey(nodeName)) {
            throw new TerminableConfigurationException(
            		"Mandatory son is missing: " + nodeName);
        }
        return sons.get(nodeName);
    }

	/**
	 * 
	 * @return
	 */
	public Collection<XmlNode> getSons() {
		Collection<XmlNode> sonValues = null;
		if(sons != null){
			sonValues = sons.values();
		}
		return sonValues;
	}
	
	/**
	 * receive collection of the sons except default entry
	 * @return
	 */
	public Collection<XmlNode> getSonsExceptDefault() {
		List<XmlNode> mySons = new ArrayList<XmlNode>(sons.size());
		for(XmlNode son: sons.values()){
			if(!son.getName().equals(DEFAULT_ENTRY) && 
			   !son.getName().equals(PARENT_PATH)){
				mySons.add(son);
			}
		}
		return mySons;
	}

	/**
	 * 
	 * @param sonNode
	 */
	public void addSon(XmlNode sonNode) {
		if(sons == null){
			sons = Collections.synchronizedMap(
					new LinkedHashMap<String, XmlNode>());
		}
		sons.put(sonNode.getName(), sonNode);
		sonNode.setParent(this);
	}
	
	/**
	 * @param configurationNode
	 */
	private void setParent(XmlNode parent) {
		this.parent = parent;
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		if(id != null){
			return new String(id);
		}else{
			return null;
		}
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * 
	 * @return parent node
	 */
	public XmlNode getParent() {
		return parent;
	}

	/**
	 * @param son name
	 */
	public XmlNode removeSon(String name) {
		return sons.remove(name);
	}
	
	public boolean hasSons(){
		return (sons != null) && !sons.isEmpty();
	}
	
	
	/**
	 *
	 */
	@Override
	public Object clone() {
		XmlNode clone = new XmlNode(
				name, value, filePath, type, isInherited);
		clone.setId(id);
		clone.setVisible(isVisible);
		clone.setParent(parent);
		if(sons != null){
			for(XmlNode sonNode : sons.values()){
				clone.addSon((XmlNode)sonNode.clone());
			}	
		}
		return clone;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return the value
	 */
	public int getValueAsInt() {
		int intValue = -1;
		try{
			intValue = Integer.parseInt(value);
		}catch(NumberFormatException nfe){
			logger.error("Error occured", nfe);
		}
		return intValue;
	}
	
	public short getValueAsShort() {
		short shortValue = -1;
		try{
			shortValue = Short.parseShort(value);
		}catch(NumberFormatException nfe){
			logger.error("Error occured", nfe);
		}
		return shortValue;
	}
	
	/**
	 * @return the value
	 */
	public int getValueAsInteger() {
		Integer intValue = null;
		try{
			intValue = Integer.valueOf(value);
		}catch(NumberFormatException nfe){
			logger.error("Error occured", nfe);
		}
		return intValue;
	}
	
	/**
	 * @return the value
	 */
	public long getValueAslong() {
		long longValue = -1;
		try{
			longValue = Long.parseLong(value);
		}catch(NumberFormatException nfe){
			logger.error("Error occured", nfe);
		}
		return longValue;
	}
	
	/**
	 * @return the value
	 */
	public Long getValueAsLong() {
		Long longValue = null;
		try{
			longValue = Long.valueOf(value);
		}catch(NumberFormatException nfe){
			logger.error("Error occured", nfe);
		}
		return longValue;
	}

    public Double getValueAsDouble() {
        double doubleValue = -1.0;
        try{
            doubleValue = Double.valueOf(value);
        }catch(NumberFormatException nfe){
            logger.error("Error occured", nfe);
        }
        return doubleValue;
    }
	
	/**
	 * @return the value
	 */
	public boolean getValueAsboolean() {
		return Boolean.parseBoolean(value);
	}
	
	/**
	 * @return the value
	 */
	public Boolean getValueAsBoolean() {
		return Boolean.valueOf(value);
	}
	
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return location son if exist
	 */
	public XmlNode getParentNode() {
		return getSon(PARENT_PATH);
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isRoot(){
		return ((value != null) && value.equals(ROOT_PATH));
	}
	
	/**
	 * @return true if the parent location entry exist
	 */
	public boolean isParentPathNode(){
		return name.equals(PARENT_PATH);
	}
	
	/**
	 * @return the isInherited
	 */
	public boolean isInherited() {
		return isInherited;
	}

	/**
	 * @param isInherited the isInherited to set
	 */
	public void setInherited(boolean isInherited) {
		this.isInherited = isInherited;
	}
	
	@Override
	public int compareTo(XmlNode o) {
		return name.compareToIgnoreCase(o.getName());
	}
	
	/**
	 * @return the isVisible
	 */
	public boolean isVisible() {
		return isVisible;
	}

	/**
	 * @param isVisible the isVisible to set
	 */
	public void setInvisible() {
		this.isVisible = false;
	}
	
	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * 
	 * @return sorted son list
	 */
	public List<XmlNode> getSortedSons() {
		List<XmlNode> list = null;
		if(sons != null){
			list = new ArrayList<XmlNode>(getSons());
			Collections.sort(list);	
		}
		return list;
	}
	
	/**
	 * @param isVisible the isVisible to set
	 */
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	/**
	 * @return the type
	 */
	public XmlType getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("XmlNode [name=").append(name).append(", value=")
				.append(value).append(", isInherited=").append(isInherited)
				.append(", isVisible=").append(isVisible).append(", parent=")
				.append(parent).append(", id=").append(id).append(", type=")
				.append(type).append(", filePath=").append(filePath)
				.append("]");
		return builder.toString();
	}
	
	public String toBriefString() {
		StringBuilder builder = new StringBuilder();
		builder.append("<name=").append(name);
		if(value != null){
			builder.append(" value=").append(value);
		}
		builder.append(">");
		return builder.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XmlNode other = (XmlNode) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}