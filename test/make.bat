@ECHO OFF
REM Batch Makefile for jazz test directory

IF "%1"=="all" GOTO all
IF "%1"=="clean" GOTO clean
IF "%1"=="EventTest" GOTO eventtest
IF "%1"=="PortalTest" GOTO portaltest
IF "%1"=="RTreeTest" GOTO rtreetest
IF "%1"=="SwingJazzTest" GOTO swingjazztest
IF "%1"=="TreeLayoutTest" GOTO treelayouttest
IF "%1"=="TwoSurfacesTest" GOTO twosurfacestest
IF "%1"=="" GOTO all

ECHO Usage: make [option]
ECHO.
ECHO Where option includes:
ECHO     all     - compiles everything
ECHO     clean   - removes .class files
ECHO     Run Jazz test Programs:
ECHO       EventTest
ECHO       PortalTest
ECHO       RTreeTest
ECHO       SwingJazzTest
ECHO       TreeLayoutTest
ECHO       TwoSurfacesTest
ECHO.
GOTO end

:all
ECHO Compiling test programs

javac -d classes -classpath ..\src\classes *.java
ECHO Build Finished
GOTO end

:clean
cd classes
del /Q *.class
cd ..
ECHO Cleaned test programs
ECHO Clean Finished
GOTO end

:eventtest
java -classpath "..\src\classes;classes" EventTest
GOTO end

:portaltest
java -classpath "..\src\classes;classes" PortalTest
GOTO end

:rtreetest
java -classpath "..\src\classes;classes" RTreeTest
GOTO end

:swingjazztest
java -classpath "..\src\classes;classes" SwingJazzTest
GOTO end

:treelayouttest
java -classpath "..\src\classes;classes" TreeLayoutTest
GOTO end

:twosurfacestest
java -classpath "..\src\classes;classes" TwoSurfacesTest
GOTO end

:end
ECHO ON
