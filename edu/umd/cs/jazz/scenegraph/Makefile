# Makefile for jazz.util packange
#

JAZZ = ../../../../..
SOURCEPATH = $(JAZZ)
COMPILER = javac
VM = java
COPTIONS = -g

CLASSES = \
	ZCamera.class \
	ZDummyVisualComponent.class \
	ZNode.class \
	ZProperty.class \
	ZRenderContext.class \
	ZRootNode.class \
	ZSelectionDecorator.class \
	ZSurface.class \
	ZTransform.class \
	ZVisualComponent.class \
	ZVisualComponentDecorator.class

all : $(CLASSES)

# Remove all class files from the package directory.
clean:
	del *.class

# Implicit rule for making Java class files from Java
# source files.
%.class: %.java
	$(COMPILER) $(COPTIONS) -sourcepath "$(SOURCEPATH)"  $*.java
