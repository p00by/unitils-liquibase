package org.unitils.liquibase;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
			LiquibaseScript annotation = getAnnotation(testMethod);
			
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
	
	private LiquibaseScript getAnnotation(Method testMethod) {
		List<LiquibaseScript> annotations = new ArrayList<LiquibaseScript>();
		LiquibaseScript liquibaseAnnotation = testMethod.getAnnotation(LiquibaseScript.class);
		if (liquibaseAnnotation != null) {
			annotations.add(liquibaseAnnotation);
		}
		for (Annotation annotation: testMethod.getAnnotations()) {
			LiquibaseScript subLiquibaseAnnotation = annotation.annotationType().getAnnotation(LiquibaseScript.class);
			if (subLiquibaseAnnotation != null) {
				annotations.add(subLiquibaseAnnotation);
			}
		}
		
		if (annotations.size() == 0) {
			return null;
		} else if (annotations.size() == 1) {
			return annotations.get(0);
		} else {
			throw new RuntimeException("Multiple liquibase annotations are not supported");
		}
		
	}
	
}