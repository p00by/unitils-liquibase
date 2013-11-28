package org.unitils.liquibase;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Before;
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
	
	@Test
	public void testOnNoAnnotation() throws Exception {
		liquibaseTestListener.beforeTestMethod(new MethodClass(), MethodClass.class.getMethod("noAnnotations"));
		
		verifyZeroInteractions(liquibaseRunner);
	}
	
	@Test
	public void testDropBeforeScript() throws Exception {
		liquibaseTestListener.beforeTestMethod(new MethodClass(), MethodClass.class.getMethod("dropBeforeScript"));
		
		verify(liquibaseRunner).dropAll();
	}
	

	@Test
	public void testExecuteScript() throws Exception {
		liquibaseTestListener.beforeTestMethod(new MethodClass(), MethodClass.class.getMethod("executeScript"));
		
		verify(liquibaseRunner).update("script");
	}
	
	@Test
	public void testDifferentBasePath() throws Exception {
		liquibaseTestListener.beforeTestMethod(new MethodClass(), MethodClass.class.getMethod("differentBasePath"));
		
		verify(liquibaseRunner).update("script", "different");
	}
	
	@Test
	public void testFirstDropThenScript() throws Exception {
		liquibaseTestListener.beforeTestMethod(new MethodClass(), MethodClass.class.getMethod("dropAndScript"));
		
		InOrder inOrder = inOrder(liquibaseRunner);

		inOrder.verify(liquibaseRunner).dropAll();
		inOrder.verify(liquibaseRunner).update("script");
	}
	
	@Test
	public void testSuperAnnotation() throws Exception {
		liquibaseTestListener.beforeTestMethod(new MethodClass(), MethodClass.class.getMethod("superAnnotation"));
		
		verify(liquibaseRunner).update("script");
	}
	
	@Test(expected = RuntimeException.class)
	public void testDoubleAnnotationThrows() throws Exception {
		liquibaseTestListener.beforeTestMethod(new MethodClass(), MethodClass.class.getMethod("doubleAnnotation"));
	}
	
	@Test
	public void testAnnotationsOnBefore() throws Exception {
		liquibaseTestListener.beforeTestSetUp(new MethodClass(), MethodClass.class.getMethod("doubleAnnotation"));
		
		verify(liquibaseRunner).update("before");
	}
	
	@Test
	public void testAnnotationsOnBeforeSuperClass() throws Exception {
		liquibaseTestListener.beforeTestSetUp(new MethodClassExtension(), MethodClass.class.getMethod("doubleAnnotation"));
		
		InOrder inOrder = inOrder(liquibaseRunner);

		inOrder.verify(liquibaseRunner).update("before");
		inOrder.verify(liquibaseRunner).update("before2");
	}
	
	@Test
	public void testBeforeClassWithSimpleAnnotation() throws Exception {
		liquibaseTestListener.beforeTestClass(MethodClass.class);
		
		verify(liquibaseRunner).update("beforeClass");
	}
	
	@Test
	public void testBeforeClassWithMetaAnnotation() throws Exception {
		liquibaseTestListener.beforeTestClass(MetaBeforeClass.class);
		
		verify(liquibaseRunner).update("script");
	}
	
	//Since you can't mock Methods using annotations, i make the methods here
	@LiquibaseScript(values = {"beforeClass"})
	public class MethodClass {
		
		@Before
		@LiquibaseScript(values = {"before"})
		public void before() {}
		
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
		
		@LiquibaseScript(values = {"script"}, basePath="different")
		public void differentBasePath() {}
		
	}
	
	public class MethodClassExtension extends MethodClass{
		@Before
		@LiquibaseScript(values = {"before2"})
		public void before2() {}
	}
	
	@SuperLiquibaseScript
	public class MetaBeforeClass {

	}
	
	@LiquibaseScript(values = {"script"})
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	public @interface SuperLiquibaseScript {
		
	}
	
	
}
