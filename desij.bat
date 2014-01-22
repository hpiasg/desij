@echo off
set DESIJ_DIR=c:\workspace\desij
echo Starting desiJ
echo current folder: %CD%

echo java -Xmx1g -jar %DESIJ_DIR%\desij.jar %1 %2 %3 %4 %5 %6 %7 %8 %9 > batch.log
java -Xmx1g -jar %DESIJ_DIR%\desij.jar %1 %2 %3 %4 %5 %6 %7 %8 %9
