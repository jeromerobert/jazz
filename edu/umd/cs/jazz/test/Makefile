# Makefile for edu.umd.cs.jazz.test packange
#

JAZZ = ../../../../..
SOURCEPATH = $(JAZZ)
COMPILER = javac
VM = java
COPTIONS = -g

CLASSES = TwoSurfaces.class 

all : $(CLASSES)

# Remove all class files from the package directory.
clean:
	del *.class

# Implicit rule for making Java class files from Java
# source files.
%.class: %.java
	$(COMPILER) $(COPTIONS) -sourcepath "$(SOURCEPATH)"  $*.java
