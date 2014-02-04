package org.unitils.liquibase.cache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class MySqlDatabaseDumper implements DatabaseDumper {

	private final String mysqlDumpPath;
	private final String user;
	private final String password;
	private final String schema;
	
	public MySqlDatabaseDumper(final String mysqlDumpPath, final String user, final String password, final String schema) {
		this.mysqlDumpPath = mysqlDumpPath;
		this.user = user;
		this.password = password;
		this.schema = schema;
	}

	@Override
	public String createDatabaseDump() throws DatabaseDumpException {
		try {
			ProcessBuilder pb = new ProcessBuilder(
	        	Arrays.asList(mysqlDumpPath, "--routines", "-u", user, "-p" + password, schema));
			Process p = pb.start();
	
	    	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
	    	StringBuilder sb = new StringBuilder();
	    	String line;
	    	while ((line = bufferedReader.readLine()) != null) {
				sb.append(line + '\n');
			}
	    	return sb.toString();
		} catch (IOException ioex) {
			throw new DatabaseDumpException(ioex);
		}
	}

}
