package org.unitils.liquibase;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.unitils.core.Module;
import org.unitils.core.TestListener;

public class LiquibaseModule implements Module {

	private static final Logger LOGGER = Logger.getLogger(LiquibaseModule.class);
	
	private LiquibaseRunner liquibaseRunner;
	
	@Override
	public void init(Properties configuration) {
		final String driver = configuration.getProperty("org.unitils.liquibase.db.driver");
		final String url = configuration.getProperty("org.unitils.liquibase.db.url");
		final String username = configuration.getProperty("org.unitils.liquibase.db.username");
		final String password = configuration.getProperty("org.unitils.liquibase.db.password");
		final String basePath = configuration.getProperty("org.unitils.liquibase.basepath");
		
		LOGGER.info(String.format("Initialized liquibase module: driver: %s, url: %s, username: %s, basepath: %s", driver, url, username, basePath));
		
		liquibaseRunner = new LiquibaseRunner(driver, url, username, password, basePath);
	}

	@Override
	public void afterInit() {
		
	}

	@Override
	public TestListener getTestListener() {
		return new LiquibaseTestListener(liquibaseRunner);
	}
	
	
	
}
