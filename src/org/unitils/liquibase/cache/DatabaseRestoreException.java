package org.unitils.liquibase.cache;

@SuppressWarnings("serial")
public class DatabaseRestoreException extends Exception {
	
	public DatabaseRestoreException(Throwable cause) {
		super(cause);
	}

	public DatabaseRestoreException(String message) {
		super(message);
	}
	
}
