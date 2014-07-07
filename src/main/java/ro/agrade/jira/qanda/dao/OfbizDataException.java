/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * File: OfbizDataException.java
 */
package ro.agrade.jira.qanda.dao;

/**
 * The exception thrown for ofbiz custom classes.
 *
 * @since 1.0
 */
public class OfbizDataException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance of <code>OfbizDataException</code> without detail
	 * message.
	 */
	public OfbizDataException() {
	}

	/**
	 * Constructs an instance of <code>OfbizDataException</code> with the
	 * specified detail message.
	 * 
	 * @param msg the detail message.
	 */
	public OfbizDataException(String msg) {
		super(msg);
	}

	/**
	 * Constructs an instance of <code>OfbizDataException</code> with the
	 * specified detail message and a cause.
	 * 
	 * @param msg the detail message.
	 * @param cause the cause.
	 */
	public OfbizDataException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
