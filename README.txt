DesiJ: A Tool for STG Decomposition

== Install ==

To run DesiJ execute bin/DesiJ (Unix) or bin\DesiJ.bat (Windows). If you want to start DesiJ with a graphical user interface, execute bin/DesiJ_gui (Unix) or bin\DesiJ_gui.bat (Windows).

To get all commandline options that DesiJ offers, execute DesiJ with the "-h" option or without any options.

For some features of DesiJ these optional software packages are needed:
Graphviz (http://www.graphviz.org/)
Ghostview (http://pages.cs.wisc.edu/~ghost/)

== Building ==

To build DesiJ, Apache Maven v3 (or later) is required.

1) Install libraries, that can't be obtained from a central Maven repository

mvn install:install-file -Dfile=./src/main/resources/lib/javailp/javailp-1.1.jar -DpomFile=./src/main/resources/lib/javailp/pom.xml
mvn install:install-file -Dfile=./src/main/resources/lib/lpsolve/lpsolve55j.jar -DpomFile=./src/main/resources/lib/lpsolve/pom.xml

2) Create executable with Maven

mvn install
