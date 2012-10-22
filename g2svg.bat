@echo off
set DESIJ_DIR=c:\workspace\desij

java -jar %DESIJ_DIR%\desij.jar operation"="convert format"="svg outfile"="%2 %1
