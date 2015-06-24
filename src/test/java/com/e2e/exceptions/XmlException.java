package com.e2e.exceptions;

@SuppressWarnings("serial")
public class XmlException extends Exception {
	
	public XmlException() {
		super();
	}

	public XmlException(String message, Throwable cause) {
		super(message, cause);
	}

	public XmlException(String message) {
		super(message);
	}

	public XmlException(Throwable cause) {
		super(cause);
	}
}
