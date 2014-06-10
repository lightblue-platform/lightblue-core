#GetAndIntallJDBCDriver
#You can check the Oracle DB version using the following query:

select * from v$version;

#Get the JDBC driver from Oracle's website (which will require you to have an account), for example:
http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html

#Then installl it accoding to the dependency described in the pom.xml of this folder
mvn install:install-file -Dfile=ojdbc6.jar -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0-4 -Dpackaging=jar

#Deploy the JDBC drive into the deployment folder and then create a jdbc connection pool
jdbc:oracle:thin:@HOST:PORT:SID

#Installing using cli:        
[standalone@localhost:9990 /] data-source add --name=lightblueOracleDS --driver-name=ojdbc6.jar --connection-url=jdbc:oracle:thin:@HOST:PORT:SID --jndi-name=java:jboss/jdbc/lightblueOracleDS --user-name=u --password=p
[standalone@localhost:9990 /] reload
[standalone@localhost:9990 /] /subsystem=datasources/data-source=lightblueOracleDS:enable()
[standalone@localhost:9990 /] /subsystem=datasources/data-source=lightblueOracleDS:test-connection-in-pool()

#Now we can access the datasource using JNDI
InitialContext context = new InitialContext();
DataSource ds = (DataSource)context.lookup("java:jboss/jdbc/lightblueOracleDS");
