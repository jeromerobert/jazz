@ECHO OFF
REM Batch Makefile for HiNote

IF "%1"=="all" GOTO all
IF "%1"=="clean" GOTO clean
IF "%1"=="rundemo" GOTO rundemo
IF "%1"=="" GOTO all

ECHO Usage: make [option]
ECHO.
ECHO Where option includes:
ECHO     all     - compiles everything
ECHO     clean   - removes .class files
ECHO     rundemo - runs HiNote Demo Program
ECHO.
GOTO end

:all
ECHO Compiling HiNote...

xcopy /S /Q resources\*.* src\classes\resources\
cd src
javac -d classes -classpath ..\..\..\src\classes *.java
cd classes
jar -cfm ..\..\hinote.jar ..\..\resources\hinote.manifest *.class resources\*.gif
cd ..\..\..\..\src\classes
jar -uf ..\..\demo\hinote\hinote.jar edu
cd ..\..\demo\hinote\resources
jar -cfm help.jar help.manifest about.jazz using.jazz
copy help.jar ..\src\classes\resources
copy help.jar ..
cd ..
ECHO Build Finished
GOTO end

:clean
cd src\classes
del /Q *.class
cd ..\..
ECHO Cleaned HiNote demo
ECHO Clean Finished
GOTO end

:rundemo
java -classpath "..\..\src\classes;src\classes" HiNote
GOTO end

:end
ECHO ON
