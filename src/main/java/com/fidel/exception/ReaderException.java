package com.fidel.exception;

import java.util.Date;

public class ReaderException extends Exception {

	private static final long serialVersionUID = -1030905209965416992L;

	public ReaderException(String message) {
		super(System.currentTimeMillis() + " : " + message);
	}

	public ReaderException(String message, int result) {
		super(new Date().toString() + " : " + message + " : " + result);
	}

	public ReaderException(Exception e) {
		super(e);
	}
}