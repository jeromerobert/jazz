@ECHO OFF
REM Batch Makefile for Jazz

IF "%1"=="all" GOTO all
IF "%1"=="clean" GOTO clean
IF "%1"=="doc" GOTO docs
IF "%1"=="docs" GOTO docs
IF "%1"=="rundemo" GOTO rundemo
IF "%1"=="" GOTO all

ECHO Usage: make [option]
ECHO.
ECHO Where option includes:
ECHO     all     - compiles everything
ECHO     clean   - removes .class files
ECHO     doc     - creates api documentation
ECHO     rundemo - runs HiNote Demo Program
ECHO.
GOTO end

:all
ECHO Compiling Jazz...
cd src
javac -d classes edu\umd\cs\jazz\component\*.java edu\umd\cs\jazz\event\*.java edu\umd\cs\jazz\io\*.java edu\umd\cs\jazz\scenegraph\*.java edu\umd\cs\jazz\util\*.java
cd classes
jar cf ../../lib/jazz.jar edu
cd ..\..
ECHO Compiling Testing code...
cd test
javac -d classes -classpath ..\src\classes *.java
cd ..
ECHO Compiling HiNote demo...
cd demo\hinote\src
javac -d classes -classpath ..\..\..\src\classes *.java
cd ..
xcopy /S /Q /I resources\*.* src\classes\resources
cd src\classes
jar -cfm ..\..\lib\hinote.jar ..\..\lib\manifest *.class resources\*.gif
cd ..\..\..\..\src\classes
jar -uf ..\..\demo\hinote\lib\hinote.jar edu
cd ..\..
ECHO Build Finished
GOTO end
:clean
cd src\classes
del /Q/S edu
cd ..\..
ECHO Cleaned Jazz
cd demo\hinote\src\classes
del /Q *
cd ..\..\..\..
ECHO Cleaned HiNote demo
cd test\classes
del /Q *
cd ..\..
ECHO Cleaned HiNote demo
ECHO Clean Finished
GOTO end

:docs
javadoc -sourcepath src -d doc\api edu.umd.cs.jazz.component edu.umd.cs.jazz.event edu.umd.cs.jazz.io edu.umd.cs.jazz.scenegraph edu.umd.cs.jazz.util 
ECHO Make Docs Completed
GOTO end

:rundemo
cd demo\hinote
java -classpath ..\..\src\classes;src\classes HiNote
cd ..\..
GOTO end

:end
ECHO ON
