package org.unitils.liquibase;

import java.sql.Connection;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class LiquibaseRunner {

	private final DataSource dataSource;
	private final ResourceAccessor resourceAccessor;
	
	public LiquibaseRunner(String driver, String url, String username, String password, String basePath) {
		dataSource = new DriverManagerDataSource(driver, url, username, password);
		resourceAccessor = new FileSystemResourceAccessor(basePath);
	}
	
	public void update(String changeLog) throws Exception {
		
		runInLiquibase(changeLog, new LiquibaseFunction() {
			@Override
			public void run(Liquibase liquibase) throws LiquibaseException {
				liquibase.update("");
			}
    	});
	}
	
	public void dropAll() throws Exception {
		runInLiquibase("", new LiquibaseFunction() {
			@Override
			public void run(Liquibase liquibase) throws LiquibaseException {
				liquibase.dropAll();
				
			}
    	});
	}
	
	private void runInLiquibase(String value, LiquibaseFunction function) throws Exception {
		Connection connection = null;
		try { 
			connection = dataSource.getConnection();
			DatabaseConnection databaseConnection = new JdbcConnection(connection);
			
			Liquibase liquibase = new Liquibase(value, resourceAccessor, databaseConnection);
			function.run(liquibase);
		} finally {
			connection.close();
		}
	}
	
	private interface LiquibaseFunction {
		
		public void run(Liquibase liquibase) throws LiquibaseException;
		
	}
	
}
