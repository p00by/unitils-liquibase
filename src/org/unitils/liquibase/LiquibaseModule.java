package org.unitils.liquibase;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.unitils.core.Module;
import org.unitils.core.TestListener;
import org.unitils.liquibase.cache.DatabaseDumper;
import org.unitils.liquibase.cache.DatabaseRestorer;
import org.unitils.liquibase.cache.MySqlDatabaseDumper;
import org.unitils.liquibase.cache.MysqlDatabaseRestorer;

import com.google.common.base.Optional;

public class LiquibaseModule implements Module {

	private static final Logger LOGGER = Logger.getLogger(LiquibaseModule.class);
	
	private LiquibaseRunner liquibaseRunner;
	private Optional<? extends DatabaseDumper> dumper;
	private Optional<? extends DatabaseRestorer> restorer;
	
	@Override
	public void init(Properties configuration) {
		final String driver = configuration.getProperty("org.unitils.liquibase.db.driver");
		final String url = configuration.getProperty("org.unitils.liquibase.db.url");
		final String username = configuration.getProperty("org.unitils.liquibase.db.username");
		final String password = configuration.getProperty("org.unitils.liquibase.db.password");
		final String basePath = configuration.getProperty("org.unitils.liquibase.basepath");
		
		LOGGER.info(String.format("Initialized liquibase module: driver: %s, url: %s, username: %s, basepath: %s", driver, url, username, basePath));
		
		liquibaseRunner = new LiquibaseRunner(driver, url, username, password, basePath);
		
		String databaseType = liquibaseRunner.getDatabaseProductName();
		
		if (databaseType.equalsIgnoreCase("MySQL")) {
			final String mysqlDumpPath = configuration.getProperty("org.unitils.mysql.mysqlDumpPath");
			final String mysqlPath = configuration.getProperty("org.unitils.mysql.mysqlPath");
			final String schema = configuration.getProperty("org.unitils.mysql.schema");
			
			dumper = Optional.of(new MySqlDatabaseDumper(mysqlDumpPath, username, password, schema));
			restorer = Optional.of(new MysqlDatabaseRestorer(mysqlPath, username, password, schema));
		} else {
			dumper = Optional.absent();
			restorer = Optional.absent();
		}
	}

	@Override
	public void afterInit() {
		
	}

	@Override
	public TestListener getTestListener() {
		return new LiquibaseTestListener(liquibaseRunner, dumper, restorer);
	}
	
	
	
}
