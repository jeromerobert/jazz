#
# This makefile works with GNU make to build HiNote
#

# Compile all the java source code
all :
	cd src; javac -d classes -classpath ../../../src/classes *.java
	cp -r resources src/classes
	cd src/classes; jar -cfm ../../hinote.jar ../../resources/hinote.manifest *.class resources/*.gif
	cd resources; jar -cfm ../src/classes/resources/help.jar ../src/classes/resources/help.manifest about.jazz using.jazz
	cp src/classes/resources/help.jar help.jar
	cd ../../src/classes; jar -uf ../../demo/hinote/hinote.jar edu

# Remove all class files
clean:
	rm -rf src/classes/*.class src/classes/resources

rundemo:
	cd src; java -classpath "../../../src/classes:classes:.." HiNote

rundemo1:
	cd src; java -classpath "../../../src/classes;classes;.." HiNote

doc: docs

docs:
	javadoc -sourcepath src -d doc/api edu.umd.cs.jazz edu.umd.cs.jazz.component edu.umd.cs.jazz.event edu.umd.cs.jazz.io edu.umd.cs.jazz.util 

tags:
	etags src/edu/umd/cs/jazz/*/*.java src/edu/umd/cs/jazz/*/*/*.java

