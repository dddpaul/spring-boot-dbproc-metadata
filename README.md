# Database procedure metadata retrieve failure
Demonstrate database function/procedure metadata retrieve failure consequences.

This project requires running Oracle instance. The simplest way to run it is to use a docker container [wnameless/docker-oracle-xe-11g](https://github.com/wnameless/docker-oracle-xe-11g). [docker-compose.yml](docker-compose.yml) is included, so you may submit `docker-compose up` command to startup Oracle XE instance. 

To satisfy dependencies grab [Oracle Database 12.1.0.2 JDBC Driver](http://www.oracle.com/technetwork/database/features/jdbc/default-2280470.html) (ojdbc7.jar) and execute:

```
mvn install:install-file -DgroupId=com.oracle -DartifactId=ojdbc7 -Dversion=12.1.0.2 -Dpackaging=jar -Dfile=ojdbc7.jar -DgeneratePom=true
```

## The problem

Spring JDBC procedure/function metadata discovery may lead to unexpected failure when database network timeouts are set. The "metadata discovery" is the ordinal SQL query performed inside `org.springframework.jdbc.core.metadata.CallMetaDataProvider` and corresponding JDBC driver. It's executed only once per database procedure. If this query is failed, for example, because of temporal database unavailability, corresponding procedure becomes unavailable until application restart.
 
There are 2 requirements to reproduce this behaviour:

* Network timeouts must be set for datasource. For example, these connections properties set timeouts for Oracle driver: `oracle.net.CONNECT_TIMEOUT=10000;oracle.jdbc.ReadTimeout=10000`.
* Procedure parameters for `SimpleJdbcCall` must be discovered implicitly (by Spring JDBC itself). It's default option.

## The possible solutions
 
And there are several ways to protect your application:

* Get rid of Spring JDBC and use plain old JDBC. It's not that bad with Java-7 [try-with-resources](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html) feature.
* Disable metadata discovery by [SimpleJdbcCall.withoutProcedureColumnMetaDataAccess()](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/simple/SimpleJdbcCall.html#withoutProcedureColumnMetaDataAccess--).
  
You have to specify parameters types explicitly in these cases.   

Links:

* [Java Oracle connectivity](https://github.com/dddpaul/java-oracle-connectivity)
* [Understanding JDBC Internals & Timeout Configuration](http://www.cubrid.org/blog/dev-platform/understanding-jdbc-internals-and-timeout-configuration/)
