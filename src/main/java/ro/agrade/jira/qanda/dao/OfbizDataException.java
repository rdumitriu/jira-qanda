/*
 * Created at Jul 23, 2010, 9:51:32 AM
 *
 * File: OfbizDataException.java
 */
package ro.agrade.jira.qanda.dao;

/**
 * The exception thrown for ofbiz custom classes.
 * 
 * @author Maria Cirtog (mcirtog@kepler-rominfo.com)
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
