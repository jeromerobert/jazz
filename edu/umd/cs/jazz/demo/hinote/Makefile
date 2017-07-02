# Makefile for HiNote, sample application
#

JAZZ = ../../../../../..
SOURCEPATH = $(JAZZ)
COMPILER = javac
VM = java
COPTIONS = -g

CLASSES = \
	CmdTable.class \
	ExtensionFileFilter.class \
	HiNote.class \
	HiNoteApplet.class \
	HiNoteBufferedInputStream.class \
	HiNoteCore.class \
	PanEventHandler.class \
	RectEventHandler.class \
	SquiggleEventHandler.class \
	TextEventHandler.class

all : $(CLASSES)

# Remove all class files from the package directory.
clean:
	del *.class

run:
	java -classpath ../../../../../.. edu.umd.cs.jazz.demo.hinote.HiNote

# This file needs to be copied to the directory containing edu
# and them 'make jar' to make the hinote jar file.
# Then, to deploy the applet, put this jar file with hinoteapplet.html
# and the help files (*.jazz) in one directory, and that's it.
jar:
	jar cvf hinote.jar \
		edu/umd/cs/jazz/component/*.class \
		edu/umd/cs/jazz/event/*.class \
		edu/umd/cs/jazz/external/jpeg/*.class \
		edu/umd/cs/jazz/io/*.class \
		edu/umd/cs/jazz/scenegraph/*.class \
		edu/umd/cs/jazz/util/*.class \
		edu/umd/cs/jazz/demo/hinote/*.class \
		edu/umd/cs/jazz/demo/hinote/cursors/*.gif \
		edu/umd/cs/jazz/demo/hinote/icons/*.gif \
		edu/umd/cs/jazz/demo/hinote/images/*.gif

# Implicit rule for making Java class files from Java
# source files.
%.class: %.java
	$(COMPILER) $(COPTIONS)  -sourcepath "$(SOURCEPATH)" $*.java
