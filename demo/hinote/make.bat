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

REM Compile HiNote source .java files, importing jazz classes above
cd src
javac -d classes -classpath ..\..\..\src\classes *.java

REM Create demo/hinote/hinote.jar, including hinote manifest file
cd classes
jar -cfm ..\..\hinote.jar ..\..\resources\hinote.manifest *.class

REM Add resources to hinote.jar
cd ..\..
jar -uf hinote.jar resources\*.gif

REM Add jazz classes to demo/hinote/hinote.jar
cd ..\..\src\classes
jar -uf ..\..\demo\hinote\hinote.jar edu

REM Create demo/hinote/resources/help.jar containing "about.jazz" and "using.jazz"
cd ..\..\demo\hinote\resources
jar -cfm help.jar help.manifest about.jazz using.jazz

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
java -classpath "..\..\src\classes;src\classes;." HiNote
GOTO end

:end
ECHO ON
