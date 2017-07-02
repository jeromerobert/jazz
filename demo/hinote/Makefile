#
# This makefile works with GNU make to build HiNote
#

# Compile all the java source code
all :
# Compile HiNote source .java files, importing jazz classes above
	cd src; javac -g -d classes -classpath ../../../src/classes *.java

# Create demo/hinote/hinote.jar, including hinote manifest file and all gif resources
	cd src/classes; jar -cfm ../../hinote.jar ../../resources/hinote.manifest *.class

# Add resources to hinote.jar
	jar -uf hinote.jar resources/*.gif

# Create demo/hinote/resources/help.jar containing "about.jazz" and "using.jazz"
	cd resources; jar -cfm ../help.jar help.manifest about.jazz using.jazz;

# Add jazz classes to demo/hinote/hinote.jar
	cd ../../src/classes; jar -uf ../../demo/hinote/hinote.jar edu

# Remove all class files
clean:
	rm -rf src/classes/*.class

rundemo:
	cd src; java -classpath "../../../src/classes:classes:.." HiNote

rundemo1:
	cd src; java -classpath "../../../src/classes;classes;.." HiNote

doc: docs

docs:
	javadoc -sourcepath src -d doc/api edu.umd.cs.jazz edu.umd.cs.jazz.component edu.umd.cs.jazz.event edu.umd.cs.jazz.io edu.umd.cs.jazz.util 

tags:
	etags src/edu/umd/cs/jazz/*/*.java src/edu/umd/cs/jazz/*/*/*.java

