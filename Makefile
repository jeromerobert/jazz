#
# This makefile works with GNU make to build Jazz and
# the demo applications
#

# Compile all the java source code
all :
	cd src; javac -d classes edu/umd/cs/jazz/component/*.java edu/umd/cs/jazz/event/*.java edu/umd/cs/jazz/io/*.java edu/umd/cs/jazz/scenegraph/*.java edu/umd/cs/jazz/util/*.java 
	cd test; javac -d classes -classpath ../src/classes *.java
	$(MAKE) -C demo/hinote
	cd src/classes; jar cf ../../lib/jazz.jar edu

# Remove all class files
clean:
	rm -rf src/classes/edu
	rm -rf demo/hinote/classes/*.class
	rm -rf test/classes/*.class

rundemo:
	java -classpath "src/classes:demo/hinote/src/classes" HiNote

doc: docs

docs:
	javadoc -sourcepath src -d doc/api edu.umd.cs.jazz.component edu.umd.cs.jazz.event edu.umd.cs.jazz.io edu.umd.cs.jazz.scenegraph edu.umd.cs.jazz.util 

tags:
	etags src/edu/umd/cs/jazz/*/*.java src/edu/umd/cs/jazz/*/*/*.java
