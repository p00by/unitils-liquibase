package org.unitils.liquibase.cache;

public interface DatabaseDumper {

	String createDatabaseDump() throws DatabaseDumpException;

}