package org.unitils.liquibase;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.liquibase.annotation.LiquibaseScript;

@RunWith(MockitoJUnitRunner.class)
public class LiquibaseTestListenerTest {

	@Mock
	private LiquibaseRunner liquibaseRunner;
	@InjectMocks
	private LiquibaseTestListener liquibaseTestListener;
	
	@Mock
	private Object testObject;
	
	@Test
	public void testOnNoAnnotation() throws Exception {
		liquibaseTestListener.beforeTestMethod(testObject, MethodClass.class.getMethod("noAnnotations"));
		
		verifyZeroInteractions(liquibaseRunner);
	}
	
	@Test
	public void testDropBeforeScript() throws Exception {
		liquibaseTestListener.beforeTestMethod(testObject, MethodClass.class.getMethod("dropBeforeScript"));
		
		verify(liquibaseRunner).dropAll();
	}
	

	@Test
	public void testExecuteScript() throws Exception {
		liquibaseTestListener.beforeTestMethod(testObject, MethodClass.class.getMethod("executeScript"));
		
		verify(liquibaseRunner).update("script");
	}
	
	@Test
	public void testFirstDropThenScript() throws Exception {
		liquibaseTestListener.beforeTestMethod(testObject, MethodClass.class.getMethod("dropAndScript"));
		
		InOrder inOrder = inOrder(liquibaseRunner);

		inOrder.verify(liquibaseRunner).dropAll();
		inOrder.verify(liquibaseRunner).update("script");
	}
	
	@Test
	public void testSuperAnnotation() throws Exception {
		liquibaseTestListener.beforeTestMethod(testObject, MethodClass.class.getMethod("superAnnotation"));
		
		verify(liquibaseRunner).update("script");
	}
	
	@Test(expected = RuntimeException.class)
	public void testDoubleAnnotationThrows() throws Exception {
		liquibaseTestListener.beforeTestMethod(testObject, MethodClass.class.getMethod("doubleAnnotation"));
	}
	
	//Since you can't mock Methods using annotations, i make the methods here
	public class MethodClass {
		
		public void noAnnotations() {}
		
		@LiquibaseScript(values = {}, dropBeforeScript = true)
		public void dropBeforeScript() {}
		
		@LiquibaseScript(values = {"script"})
		public void executeScript() {}
		
		@LiquibaseScript(values = {"script"}, dropBeforeScript = true)
		public void dropAndScript() {}
		
		@SuperLiquibaseScript
		public void superAnnotation() {}
		
		@LiquibaseScript(values = {"script"}, dropBeforeScript = true)
		@SuperLiquibaseScript
		public void doubleAnnotation() {}
		
	}
	
	@LiquibaseScript(values = {"script"})
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	public @interface SuperLiquibaseScript {
		
	}
	
	
}
