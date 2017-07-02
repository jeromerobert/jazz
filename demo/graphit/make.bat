@ECHO OFF
REM Batch Makefile for GraphIt

IF "%1"=="all" GOTO all
IF "%1"=="clean" GOTO clean
IF "%1"=="rundemo" GOTO rundemo
IF "%1"=="" GOTO all

ECHO Usage: make [option]
ECHO.
ECHO Where option includes:
ECHO     all     - compiles everything
ECHO     clean   - removes .class files
ECHO     rundemo - runs GraphIt Demo Program
ECHO.
GOTO end

:all
ECHO Compiling GraphIt...
cd src
javac -d classes -classpath ..\..\..\src\classes *.java
cd classes
jar -cf ..\..\graphit.jar *.class
cd ..\..
jar -uf graphit.jar resources\*.gif
cd ..\..\src\classes
jar -uf ..\..\demo\graphit\graphit.jar edu
cd ..\..\demo\graphit
ECHO Build Finished
GOTO end

:clean
cd src\classes
del /Q *.class
cd ..\..
ECHO Cleaned GraphIt demo
ECHO Clean Finished
GOTO end

:rundemo
appletviewer graphit.html
GOTO end

:end
ECHO ON
