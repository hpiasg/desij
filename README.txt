DesiJ: A Tool for STG Decomposition

== Install ==

DesiJ requires a Java runtime environment (JRE) v1.6 (or later) in order to run. To start DesiJ execute bin/DesiJ (Unix) or bin\DesiJ.bat (Windows). If you want to start DesiJ with a graphical user interface, execute bin/DesiJ_gui (Unix) or bin\DesiJ_gui.bat (Windows).

To list all commandline options that DesiJ offers, execute DesiJ with the "-h" option or without any options.

For some features of DesiJ these software packages may be required:
Graphviz (http://www.graphviz.org/)
Ghostview (http://pages.cs.wisc.edu/~ghost/)

== Build ==

To build DesiJ, Apache Maven v3 (or later) and the Java Development Kit (JDK) v1.6 (or later) are required.

1) Install libraries, that can't be obtained from a central Maven repository

mvn install:install-file -Dfile=./src/main/resources/lib/javailp/javailp-1.1.jar -DpomFile=./src/main/resources/lib/javailp/pom.xml
mvn install:install-file -Dfile=./src/main/resources/lib/lpsolve/lpsolve55j.jar -DpomFile=./src/main/resources/lib/lpsolve/pom.xml

2) Create executable with Maven

mvn install
