package com.e2e.exceptions;

public class TerminableConfigurationException extends RuntimeException {
	private static final long serialVersionUID = -6948642310785174189L;

	private Type type = Type.configuration;

	public enum Type {
		configuration, recovery
	}

	public TerminableConfigurationException() {
		super();
	}

	public TerminableConfigurationException(String message) {
		super(message);
	}

	public TerminableConfigurationException(Type type, String message) {
		super(message);
		this.type = type;
	}

	public TerminableConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @return type name as string
	 */
	public String getType() {
		return this.type.name();
	}

	public TerminableConfigurationException(Throwable cause) {
		super(cause);
	}

}
