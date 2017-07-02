# Makefile for jazz.util packange
#

JAZZ = ../../../../..
SOURCEPATH = $(JAZZ)
COMPILER = javac
VM = java
COPTIONS = -g

CLASSES = \
	ZConstraintDecorator.class \
	ZImage.class \
	ZLinkDecorator.class \
	ZPolyline.class \
	ZRectangle.class \
	ZStickyZDecorator.class \
	ZStickyDecorator.class \
	ZText.class

all : $(CLASSES)

# Remove all class files from the package directory.
clean:
	del *.class

# Implicit rule for making Java class files from Java
# source files.
%.class: %.java
	$(COMPILER) $(COPTIONS) -sourcepath "$(SOURCEPATH)"  $*.java
