package org.unitils.liquibase.cache;

import java.util.Arrays;

public class MysqlDatabaseRestorer implements DatabaseRestorer {

	private final String mysqlPath;
	private final String user;
	private final String password;
	private final String schema;
	
	public MysqlDatabaseRestorer(final String mysqlPath, final String user, final String password, final String schema) {
		this.mysqlPath = mysqlPath;
		this.user = user;
		this.password = password;
		this.schema = schema;
	}
	
	@Override
	public void restoreDatabase(final String dump) throws DatabaseRestoreException {
		try { 
			ProcessBuilder pb = new ProcessBuilder(
				Arrays.asList(mysqlPath, "-u", user, "-p" + password, schema));
	    	pb.redirectErrorStream(true);
			Process p = pb.start();
	    	p.getOutputStream().write(dump.getBytes());
	    	p.getOutputStream().close();
	    	p.waitFor();
		} catch (Exception ex) {
			throw new DatabaseRestoreException(ex);
		}
	}
	
	
	
}
