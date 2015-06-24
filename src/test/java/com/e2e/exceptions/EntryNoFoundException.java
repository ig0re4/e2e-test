package com.e2e.exceptions;

@SuppressWarnings("serial")
public class EntryNoFoundException extends Exception {
	String entryPath;	
	public EntryNoFoundException(String entryPath){
		this.entryPath = entryPath;
	}

	@Override
	public String toString() {
		return "Failed to find entry at path: " + this.entryPath;
	}
	
	@Override 
	public String getMessage() {
		return this.toString();
	}

}
