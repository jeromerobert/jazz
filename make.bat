@ECHO OFF
REM Batch Makefile for Jazz

IF "%1"=="all" GOTO all
IF "%1"=="clean" GOTO clean
IF "%1"=="doc" GOTO docs
IF "%1"=="hinote" GOTO hinote
IF "%1"=="graphit" GOTO graphit
IF "%1"=="docs" GOTO docs
IF "%1"=="runhinote" GOTO runhinote
IF "%1"=="rungraphit" GOTO rungraphit
IF "%1"=="" GOTO all

ECHO Usage: make [option]
ECHO.
ECHO Where option includes:
ECHO     all        - compiles everything
ECHO     clean      - removes .class files
ECHO     doc        - creates api documentation
ECHO     hinote     - compiles just the HiNode demo
ECHO     graphit    - compiles just the GraphIt demo
ECHO     runhinote  - runs HiNote Demo Program
ECHO     rungraphit - runs GraphIt Demo Program
ECHO.
GOTO end

:all
ECHO Compiling Jazz...
cd src
javac -O -d classes edu\umd\cs\jazz\*.java edu\umd\cs\jazz\component\*.java edu\umd\cs\jazz\event\*.java edu\umd\cs\jazz\io\*.java edu\umd\cs\jazz\util\*.java
cd classes
jar cf ../../lib/jazz.jar edu
cd ..\..
ECHO Compiling Testing code...
cd test
javac -O -d classes -classpath ..\src\classes *.java
cd ..

ECHO Compiling HiNote demo...
REM Compile HiNote source .java files, importing jazz classes above
cd demo\hinote\src
javac -O -d classes -classpath ..\..\..\src\classes *.java

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

copy help.jar ..
cd ..\..\..

ECHO Compiling GraphIt...
cd demo\graphit\src
javac -d classes -classpath ..\..\..\src\classes *.java
cd classes
jar -cf ..\..\graphit.jar *.class
cd ..\..
jar -uf graphit.jar resources\*.gif
cd ..\..\src\classes
jar -uf ..\..\demo\graphit\graphit.jar edu
cd ..\..
ECHO Build Finished
GOTO end

:hinote
ECHO Compiling HiNote demo...
cd demo\hinote

REM Compile HiNote source .java files, importing jazz classes above
cd src
javac -O -d classes -classpath ..\..\..\src\classes *.java

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

copy help.jar ..
cd ..\..\..
ECHO Build Finished
GOTO end

:graphit
ECHO Compiling GraphIt...
cd demo\graphit\src
javac -d classes -classpath ..\..\..\src\classes *.java
cd classes
jar -cf ..\..\graphit.jar *.class
cd ..\..
jar -uf graphit.jar resources\*.gif
cd ..\..\src\classes
jar -uf ..\..\demo\graphit\graphit.jar edu
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
cd demo\graphit\src\classes
del /Q *
cd ..\..\..\..
ECHO Clean GraphIt demo
cd test\classes
del /Q *
cd ..\..
ECHO Cleaned Test files
ECHO Clean Finished
GOTO end

:docs
javadoc -sourcepath src -use -doctitle "Jazz API Documentation" -windowtitle "Jazz API Documentation" -header "Jazz API Documentation" -footer "Jazz API Documentation" -d doc\api edu.umd.cs.jazz edu.umd.cs.jazz.component edu.umd.cs.jazz.event edu.umd.cs.jazz.io edu.umd.cs.jazz.util 
ECHO Make Docs Completed
GOTO end

:runhinote
cd demo\hinote
java -classpath ..\..\src\classes;src\classes;. HiNote
cd ..\..
GOTO end

:rungraphit
cd demo\graphit
appletviewer graphit.html
cd ..\..
GOTO end

:end
ECHO ON






