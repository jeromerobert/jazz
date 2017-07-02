#
# This makefile works with GNU make to build Jazz test directory programs
#

# Compile all the java source code
all :
	javac -g -d classes -classpath ../src/classes *.java

# Remove all class files
clean:
	rm -rf classes/*.class

EventTest:
	java -classpath "../src/classes:classes:.." EventTest

PortalTest:
	java -classpath "../src/classes:classes:.." PortalTest

RTreeTest:
	java -classpath "../src/classes:classes:.." RTreeTest

SwingJazzTest:
	java -classpath "../src/classes:classes:.." SwingJazzTest

TreeLayoutTest:
	java -classpath "../src/classes:classes:.." TreeLayoutTest

TwoSurfacesTest:
	java -classpath "../src/classes:classes:.." TwoSurfacesTest

DragAndDropTest:
	java -classpath "../src/classes:classes:.." DragAndDropTest

