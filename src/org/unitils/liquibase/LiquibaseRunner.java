package org.unitils.liquibase;

import java.sql.Connection;
import java.sql.SQLException;

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
	private final String basePath;
	
	public LiquibaseRunner(String driver, String url, String username, String password, String basePath) {
		dataSource = new DriverManagerDataSource(driver, url, username, password);
		this.basePath = basePath;
	}
	
	public void update(String changeLog) throws Exception {
		runInLiquibase(changeLog, new LiquibaseFunction() {
			@Override
			public void run(Liquibase liquibase) throws LiquibaseException {
				liquibase.update("");
			}
    	});
	}
	
	public void update(String changeLog, String basePath) throws Exception {
		runInLiquibase(changeLog, new LiquibaseFunction() {
			@Override
			public void run(Liquibase liquibase) throws LiquibaseException {
				liquibase.update("");
			}
    	}, basePath);
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
		runInLiquibase(value, function, basePath);
	}
	
	private void runInLiquibase(String value, LiquibaseFunction function, String basePath) throws Exception {
		Connection connection = null;
		try { 
			connection = dataSource.getConnection();
			DatabaseConnection databaseConnection = new JdbcConnection(connection);
			ResourceAccessor resourceAccessor = new FileSystemResourceAccessor(basePath);
			
			Liquibase liquibase = new Liquibase(value, resourceAccessor, databaseConnection);
			function.run(liquibase);
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}
	
	public String getDatabaseProductName() {
		Connection connection = null;
		try { 
			connection = dataSource.getConnection();
			DatabaseConnection databaseConnection = new JdbcConnection(connection);
			return databaseConnection.getDatabaseProductName();
		} catch (Exception ex) {
			return null;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					//ignore
				}
			}
		}
	}
	
	private interface LiquibaseFunction {
		
		public void run(Liquibase liquibase) throws LiquibaseException;
		
	}
	
}
