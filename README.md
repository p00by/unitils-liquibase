unitils-liquibase
=================

Plugin for unitils to support liquibase script execution. The core annotation is @LiquibaseScript

It can be put before a test method like this:

Basic usage
===========

```java
@LiquibaseScript(
		dropBeforeScript = true,
		values = {"script1, script2"})
@Test
public void test() {
}
```

Or before an @Before method

```java
@LiquibaseScript(
		dropBeforeScript = true,
		values = {"script1, script2"})
@Before
public void before() {
}
```

Meta-annotations
================

Also has some support for meta-annotations

```java
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@LiquibaseScript(
		dropBeforeScript = true,
		values = {"create-tables.xml"})
public @interface EmptyDatabase {

}
```

Which you can then use instead of the @LiquibaseScript annotation

```
@Test
@EmptyDatabase
public void test() {
}
```


Configuration
=============

Add this to your unitils.properties
```
unitils.modules=...,liquibase

unitils.module.liquibase.className=org.unitils.liquibase.LiquibaseModule
unitils.module.liquibase.runAfter=
unitils.module.liquibase.enabled=true

org.unitils.liquibase.db.driver=?
org.unitils.liquibase.db.url=?
org.unitils.liquibase.db.username=?
org.unitils.liquibase.db.password=?
org.unitils.liquibase.basepath=? (liquibase script location)
```
