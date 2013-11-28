package org.unitils.liquibase;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.unitils.core.TestListener;
import org.unitils.core.UnitilsException;
import org.unitils.liquibase.annotation.LiquibaseScript;


public class LiquibaseTestListener extends TestListener {

	private static final Logger LOGGER = Logger.getLogger(LiquibaseTestListener.class);
	private final LiquibaseRunner liquibaseRunner;
	
	public LiquibaseTestListener(LiquibaseRunner liquibaseRunner) {
		this.liquibaseRunner = liquibaseRunner; 
	}
	
	@Override
	public void beforeTestSetUp(Object testObject, Method testMethod) {
		Class<?> testObjectClass = testObject.getClass();
		List<Class<?>> classes = new ArrayList<Class<?>>();
		while (!testObjectClass.equals(Object.class)) {
			classes.add(testObjectClass);
			testObjectClass = testObjectClass.getSuperclass();
		}
		for (int i = classes.size() - 1; i >= 0; i--) {
			testObjectClass = classes.get(i);
			for (Method beforeMethod: testObjectClass.getDeclaredMethods()) {
				if (beforeMethod.getAnnotation(Before.class) != null) {
					runLiquibase(beforeMethod);
				}
			}
		}
	}
	
	@Override
	public void beforeTestMethod(Object testObject, Method testMethod) {
		runLiquibase(testMethod);
	}
	
	@Override
	public void beforeTestClass(Class<?> testClass) {
		LiquibaseScript annotation = getLiquibaseAnnotation(testClass.getAnnotations());
		if (annotation != null) {
			runLiquibase(annotation);
		}
	}
	
	private void runLiquibase(Method testMethod) {
		LiquibaseScript annotation = getLiquibaseAnnotation(testMethod.getAnnotations());
		if (annotation != null) {
			runLiquibase(annotation);
		}
	}
	
	private void runLiquibase(LiquibaseScript annotation) {
		try {
	    	if (annotation.dropBeforeScript()) {
	        	LOGGER.debug("Dropping everything");
	        	liquibaseRunner.dropAll();
	    	}
	        for (String value: annotation.values()) {
	        	LOGGER.debug("Running liquibase script" + value);
	        	liquibaseRunner.update(value);
	        }
		} catch (Exception ex) {
			LOGGER.error("Could not run liquibase command", ex);
			throw new UnitilsException(ex);
		}
	}
	
	private LiquibaseScript getLiquibaseAnnotation(Annotation[] annotations) {
		List<LiquibaseScript> liquibaseAnnotations = new ArrayList<LiquibaseScript>();
		for (Annotation annotation: annotations) {
			if (annotation.annotationType().equals(LiquibaseScript.class)) {
				liquibaseAnnotations.add((LiquibaseScript) annotation);
			} else {
				LiquibaseScript subLiquibaseAnnotation = annotation.annotationType().getAnnotation(LiquibaseScript.class);
				if (subLiquibaseAnnotation != null) {
					liquibaseAnnotations.add(subLiquibaseAnnotation);
				}
			}
		}
		
		if (liquibaseAnnotations.size() == 0) {
			return null;
		} else if (liquibaseAnnotations.size() == 1) {
			return liquibaseAnnotations.get(0);
		} else {
			throw new RuntimeException("Multiple liquibase annotations are not supported");
		}
		
	}
	
}