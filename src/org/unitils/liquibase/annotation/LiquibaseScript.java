package org.unitils.liquibase.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface LiquibaseScript {

	String[] values();
	boolean dropBeforeScript() default true;
	String basePath() default "";
	
}
