
Install external Libs:

cd [BASEDIR]/src/main/resources/lib/javailp
mvn install:install-file -Dfile=javailp-1.1.jar -DpomFile=pom.xml 

cd [BASEDIR]/src/main/resources/lib/lpsolve
mvn install:install-file -Dfile=lpsolve55j.jar -DpomFile=pom.xml 
