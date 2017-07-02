# Makefile for jazz.util packange
#

JAZZ = ../../../../..
SOURCEPATH = $(JAZZ)
COMPILER = javac
VM = java
COPTIONS = -g

CLASSES = \
	Dimension2DFloat.class \
	ZArea.class \
	ZBounds.class \
	ZBasicApplet.class \
	ZBasicComponent.class \
	ZBasicFrame.class \
	ZBasicWindow.class \
	ZFindFilterBounds.class \
	ZFindFilterMagBounds.class \
	ZOperationNotAllowedException.class \
	ZUtil.class \
	ZDebug.class

all : $(CLASSES)

# Remove all class files from the package directory.
clean:
	del *.class

# Implicit rule for making Java class files from Java
# source files.
%.class: %.java
	$(COMPILER) $(COPTIONS)  -sourcepath "$(SOURCEPATH)" $*.java
