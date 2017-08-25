for run scripts :


mvn clean

mvn install

cd target/refphoto-0.0.1-SNAPSHOT/WEB-INF/classes

export CLASSPATH=.:`pwd`/../lib/log4j-1.2.17.jar:`pwd`/../lib/spymemcached-2.12.3.jar:`pwd`/../lib/commons-lang-2.6.jar:`pwd`/../lib/mysql-connector-java-5.1.38.jar

java lille3.refphoto.cli.Importuser <uid>
(where uid is the uid of the person, example : java lille3.refphoto.cli.Importuser 1940)

java lille3.refphoto.cli.Importall
(for import all photos for all person in the ldap)

