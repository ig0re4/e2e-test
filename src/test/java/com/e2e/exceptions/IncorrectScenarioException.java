package com.e2e.exceptions;

@SuppressWarnings("serial")
public class IncorrectScenarioException extends Exception {
	public IncorrectScenarioException(String message) {
		super(message);
	}
}
