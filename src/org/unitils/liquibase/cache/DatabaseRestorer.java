package org.unitils.liquibase.cache;

public interface DatabaseRestorer {

	void restoreDatabase(String dump) throws DatabaseRestoreException;
	
}
