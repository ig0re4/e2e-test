package com.e2e.exceptions;

@SuppressWarnings("serial")
public class IncorrectCommandException extends Exception {
	//message
	private String message;
	
	/**
	 * 
	 * @param txt
	 */
	public IncorrectCommandException(String[] txt) {
		StringBuilder builder = new StringBuilder();
		for(String str : txt) {
		    builder.append(str);
		}
		message = builder.toString();
	}

	/**
	 * 
	 * @param message
	 */
	public IncorrectCommandException(String message) {
		this.message = message;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IncorrectCommandException [message=").append(message)
				.append("]");
		return builder.toString();
	}

}
