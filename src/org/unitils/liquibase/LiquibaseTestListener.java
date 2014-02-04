package org.unitils.liquibase;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.unitils.core.TestListener;
import org.unitils.core.UnitilsException;
import org.unitils.liquibase.annotation.LiquibaseScript;
import org.unitils.liquibase.cache.DatabaseDumpException;
import org.unitils.liquibase.cache.DatabaseDumper;
import org.unitils.liquibase.cache.DatabaseRestoreException;
import org.unitils.liquibase.cache.DatabaseRestorer;

import com.google.common.base.Optional;


public class LiquibaseTestListener extends TestListener {
	
	private Map<LiquibaseScript, String> dumps = new HashMap<LiquibaseScript, String>();

	private static final Logger LOGGER = Logger.getLogger(LiquibaseTestListener.class);
	private final LiquibaseRunner liquibaseRunner;
	private final Optional<? extends DatabaseDumper> databaseDumper;
	private final Optional<? extends DatabaseRestorer> databaseRestorer;
	
	public LiquibaseTestListener(LiquibaseRunner liquibaseRunner,
			Optional<? extends DatabaseDumper> databaseDumper, 
			Optional<? extends DatabaseRestorer> databaseRestorer) {
		this.liquibaseRunner = liquibaseRunner; 
		this.databaseDumper = databaseDumper;
		this.databaseRestorer = databaseRestorer;
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
	    	if (annotation.cacheDatabase() && dumps.containsKey(annotation)) {
	    		LOGGER.info("Restoring database from cache");
	    		restoreDatabase(annotation);
        	} else {
		        for (String value: annotation.values()) {
		        	LOGGER.debug("Running liquibase script" + value);
		        	if ("".equals(annotation.basePath())) {
		        		liquibaseRunner.update(value);
		        	} else {
		        		liquibaseRunner.update(value, annotation.basePath());
		        	}
		        }
		        if (annotation.cacheDatabase()) {
		    		LOGGER.info("Caching database");
		        	cacheDatabase(annotation);
		        }
        	}
		} catch (Exception ex) {
			LOGGER.error("Could not run liquibase command", ex);
			throw new UnitilsException(ex);
		}
	}
	
	private void restoreDatabase(LiquibaseScript annotation) throws DatabaseRestoreException {
		String dump = dumps.get(annotation);
		if (databaseRestorer.isPresent()) {
		 	databaseRestorer.get().restoreDatabase(dump);
		} else {
			LOGGER.warn("Not able to restore this database since no restorer is configured");
		}
	}

	private void cacheDatabase(LiquibaseScript liquibaseScript) throws DatabaseDumpException {
		if (databaseDumper.isPresent()) {
    		if (liquibaseScript.dropBeforeScript()) {
				String dump = databaseDumper.get().createDatabaseDump();
	    		dumps.put(liquibaseScript, dump);
    		} else {
    			LOGGER.warn("Will only cache databases that are freshly created (drop before script = true)");
    		}
    	} else {
    		LOGGER.warn("No database dump implementation present, not able to cache this database");
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