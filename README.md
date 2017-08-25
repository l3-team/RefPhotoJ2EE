Referencial Photos in J2EE

Webservice REST which allow provide the ID photo for a person (from personal datas LDAP) 

Allow :
---
- provide the personal photo of one user to an authorized application (by ip address or dns host) ;
- from a security token generated on demand (usable only once, valid for 2 minutes) obtained from ID LDAP user (uid), or student card number (supannEtuId), or employee ID number (supannEmpId) ;
- according to the choice of the user stored in a field on LDAP (usePhoto : TRUE or FALSE). If TRUE, the photo of the user can be returned. If FALSE, the default photo with text "authorization refused" is returned ; 

The storage :
---
- one side in metadatas (database stored the ID LDAP user (uid) and the fingerprint SHA1 of the photo) ;
- other side in binaries (the path of the stored image is builted from the fingerprint SHA1 of the photo, example: if the fingerprint SHA1 is 8a7b908fdac1eedc8acc8f7758f19a33faf2eb72 then the photo will be stored in 8a/7b/908fdac1eedc8acc8f7758f19a33faf2eb72.jpg) ;

Client side uses :
---
- Photo in applications symfony 2 : https://github.com/l3-team/PhotoBundle
- Photo in esup-mon-dossier-web version2 : https://github.com/l3-team/Lille3PhotoEsupMonDossierWebV2
- Photo in esup-mon-dossier-web version3 : https://github.com/l3-team/Lille3PhotoEsupMonDossierWebV3
- Photo with routes /binary*

How it works (two steps use) :
---
- first the route /token/add/{uid} is accessed only for authorized application (example : http://server/refphoto/token/add/P7279), it prints the token valid for 2 minutes (example : c4ca4238a0b923820dcc509a6f75849b) ;
- then the route /image/{token} is accessed for public (example : http://server/refphoto/image/c4ca4238a0b923820dcc509a6f75849b), it shows the photo of the user of not (according the value TRUE ou FALSE of the LDAP field usePhoto on the LDAP record for the user) ;

Other routes :
---
- the route /tokenEtu/add/{supannEtuId} is accessed only for authorized application (example : http://server/refphoto/tokenEtu/add/8877665544), it prints the token valid for 2 minutes (example : c4ca4238a0b923820dcc509a6f75849b) ;
- the route /tokenPers/add/{supannEmpId} is accessed only for authorized application (example : http://server/refphoto/tokenPers/add/3007279), it prints the token valid for 2 minutes (example : c4ca4238a0b923820dcc509a6f75849b) ;
- the route /binary/{uid} is accessed only for authorized application (example : http://server/refphoto/binary/P7279), it shows the photo of the user (according his choice if the LDAP field usePhoto) ;
- the route /binaryEtu/{supannEtuId} is accessed only for authorized application (example : http://server/refphoto/binaryEtu/8877665544), it shows the photo of the user (according the LDAP field usePhoto) ;
- the route /binaryPers/{supannEmpId} is accessed only for authorized application (example : http://server/refphoto/binaryPers/3007279), it shows the photo of the user (according 

Pre-requisites :
---
* JDK 8 (1.8.0_131 used)
* TOMCAT8 webserver (which can run Spring application) ;
* LDAP directory with schema SUPANN (with fields, uid, eduPersonAffiliation, eduPersonPrimaryAffiliation, supannEtuId and supannEmpId) ;
* LDAP field usePhoto (with possibles values TRUE or FALSE) ;
* MySQL database
* Memcached daemon
* Directory datas with JPEG Photos (to rename like : {supannEmpId}.jpg (for an employee person) or {supannEtuId}.jpg (for a student person) ;
* Directory binaries with write ACL unix for webserver (user tomcat8 or tomcat) ;
* The user unix of the Tomcat (user tomcat8 or tomcat) needs to have write access on /tmp directory (chmod 777 /tmp)
* List for ip address of dns host for the authorized applications ;
* Optionnal : Ip address for the reverse proxy (for separated DMZ networks) ;



Configuration
---
* the configuration is in  the next files :
- WebContent/WEB-INF/conf/log4j.properties       : for the logs
- WebContent/WEB-INF/conf/database.properties    : for the parameters of the database (for store the metadata) 
- WebContent/WEB-INF/conf/ldap.properties        : for the parameters of the ldap
- WebContent/WEB-INF/conf/binarystore.properties : for the configuration of the binaries stored
- WebContent/WEB-INF/conf/memcache.properties    : for the parameters of the memcache
- WebContent/WEB-INF/conf/security.properties    : for the parameters of the security (ip, reverseproxy, autorized applications)


Installation
---

* create the schema of the MySQL database :

```

mysql -h dbserver.host.domain -u root -p < sql/dump.sql

```

* configure the application (see Configuration section below)

* compile the sources
```
mvn clean
mvn install
```

* deploy the application
the war file refphoto-0.0.1-SNAPSHOT.war for the deployment in the webapp directory on the Tomcat is in target directory




Availables commands :
---
- for import the photo of the user which UID is P7279 :
```
cd target/refphoto-0.0.1-SNAPSHOT/WEB-INF/classes

export CLASSPATH=.:`pwd`/../lib/log4j-1.2.17.jar:`pwd`/../lib/spymemcached-2.12.3.jar:`pwd`/../lib/commons-lang-2.6.jar:`pwd`/../lib/mysql-connector-java-5.1.38.jar

java lille3.refphoto.cli.Importuser <uid>
(where uid is the uid of the person, example : java lille3.refphoto.cli.Importuser P7279)
```
- for loops on all LDAP user :
```
cd target/refphoto-0.0.1-SNAPSHOT/WEB-INF/classes

export CLASSPATH=.:`pwd`/../lib/log4j-1.2.17.jar:`pwd`/../lib/spymemcached-2.12.3.jar:`pwd`/../lib/commons-lang-2.6.jar:`pwd`/../lib/mysql-connector-java-5.1.38.jar

java lille3.refphoto.cli.Importall
(for import all photos for all person in the ldap)
```
- for deletes photos of missings accounts LDAP ;
```
cd target/refphoto-0.0.1-SNAPSHOT/WEB-INF/classes

export CLASSPATH=.:`pwd`/../lib/log4j-1.2.17.jar:`pwd`/../lib/spymemcached-2.12.3.jar:`pwd`/../lib/commons-lang-2.6.jar:`pwd`/../lib/mysql-connector-java-5.1.38.jar

java lille3.refphoto.cli.Delete
(for import all photos for all person in the ldap)
```



