package org.unitils.liquibase;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.unitils.core.TestListener;
import org.unitils.liquibase.annotation.LiquibaseScript;

public class LiquibaseTestListener extends TestListener {

	private static final Logger LOGGER = Logger.getLogger(LiquibaseTestListener.class);
	private final LiquibaseRunner liquibaseRunner;
	
	public LiquibaseTestListener(LiquibaseRunner liquibaseRunner) {
		this.liquibaseRunner = liquibaseRunner; 
	}
	
	@Override
	public void beforeTestMethod(Object testObject, Method testMethod) {
		try {
			LiquibaseScript annotation = testMethod.getAnnotation(LiquibaseScript.class);	
			
			if(annotation != null){
            	if (annotation.dropBeforeScript()) {
	            	LOGGER.debug("Dropping everything");
	            	liquibaseRunner.dropAll();
            	}
	            for (String value: annotation.values()) {
	            	LOGGER.debug("Running liquibase script" + value);
	            	liquibaseRunner.update(value);
	            }
            }
		} catch (Exception ex) {
			LOGGER.error("Could not run liquibase command");
			throw new RuntimeException(ex);
		}
	}
	
	
	
}
