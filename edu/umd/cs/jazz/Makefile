#
# This makefile works with GNU make to build Jazz and
# the demo applications
#

JAZZ = ../../../..
SOURCEPATH = $(JAZZ)
COMPILER = javac
VM = java
COPTIONS = -g
ROPTIONS = 

all :
	$(MAKE) -C external/jpeg
	$(MAKE) -C util
	$(MAKE) -C scenegraph
	$(MAKE) -C event
	$(MAKE) -C io
	$(MAKE) -C component
	$(MAKE) -C test
	$(MAKE) -C demo/hinote

# Remove all class files
clean:
	$(MAKE) -C external/jpeg clean
	$(MAKE) -C util clean
	$(MAKE) -C scenegraph clean
	$(MAKE) -C event clean
	$(MAKE) -C io clean
	$(MAKE) -C component clean
	$(MAKE) -C test clean
	$(MAKE) -C demo/hinote clean

rundemo:
	$(MAKE) -C demo/hinote run

doc: docs

docs:
	javadoc -d doc/api edu.umd.cs.jazz.util edu.umd.cs.jazz.scenegraph edu.umd.cs.jazz.event \
		edu.umd.cs.jazz.io edu.umd.cs.jazz.component

tags:
	etags io/*.java scenegraph/*.java component/*.java util/*.java event/*.java

# Implicit rule for making Java class files from Java
# source files.
%.class:
	$(COMPILER) $(COPTIONS) -sourcepath "$(SOURCEPATH)"  $*.java
