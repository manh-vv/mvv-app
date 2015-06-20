#Install library to maven repository local
---
> mvn install:install-file -Dfile=<path-to-file> -DgroupId=<group-id> -DartifactId=<artifact-id> -Dversion=<version> -Dpackaging=<packaging>

## + sqlite-jdbc-3.8.10.1.jar

### maven
> mvn install:install-file -Dfile="sqlite-jdbc-3.8.10.1.jar" -DgroupId="org.xerial" -DartifactId="sqlite-jdbc" -Dversion="3.8.10.1" -Dpackaging="jar"

