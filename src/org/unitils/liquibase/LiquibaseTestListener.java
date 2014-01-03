package org.unitils.liquibase;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
		for (LiquibaseScript annotation: getLiquibaseAnnotation(testClass.getAnnotations())) {
			runLiquibase(annotation);
		}
	}
	
	private void runLiquibase(Method testMethod) {
		for (LiquibaseScript annotation: getLiquibaseAnnotation(testMethod.getAnnotations())) {
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
	        	if ("".equals(annotation.basePath())) {
	        		liquibaseRunner.update(value);
	        	} else {
	        		liquibaseRunner.update(value, annotation.basePath());
	        	}
	        }
		} catch (Exception ex) {
			LOGGER.error("Could not run liquibase command", ex);
			throw new UnitilsException(ex);
		}
	}
	
	private List<LiquibaseScript> getLiquibaseAnnotation(Annotation[] annotations) {
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
		
		Collections.sort(liquibaseAnnotations, new Comparator<LiquibaseScript>() {
			@Override
			public int compare(LiquibaseScript left, LiquibaseScript right) {
				if (left.order() < right.order()) {
					return -1;
				} else if (left.order() > right.order()) {
					return 1;
				} else {
					return Integer.valueOf(left.hashCode()).compareTo(Integer.valueOf(right.hashCode()));
				}
			}
		});
		
		return liquibaseAnnotations;
	}
	
}