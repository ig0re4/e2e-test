package com.e2e.robot;

import com.e2e.xml.XmlNode;

public class Command {
	private CommandEnum code; 
	private XmlNode node;
	
	public Command(XmlNode node) {
		this.code = CommandEnum.getCommand(node.getName());
		this.node = node;
	}

	/**
	 * @return the node
	 */
	public XmlNode getNode() {
		return node;
	}
	
	/**
	 * 
	 * @return
	 */
	public CommandEnum getCode() {
		return code;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
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
		Command other = (Command) obj;
		if (code != other.code)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Command [code=").append(code).append(", node=")
				.append(node).append("]");
		return builder.toString();
	}
}

