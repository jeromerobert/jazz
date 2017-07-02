#
# This makefile works with GNU make to build Jazz and
# the demo applications
#

# Compile all the java source code
all :
	cd src; javac -g -d classes edu/umd/cs/jazz/*.java edu/umd/cs/jazz/component/*.java edu/umd/cs/jazz/event/*.java edu/umd/cs/jazz/io/*.java edu/umd/cs/jazz/util/*.java 
	$(MAKE) -C test
	$(MAKE) -C demo/hinote
	$(MAKE) -C demo/graphit
	cd src/classes; jar cf ../../lib/jazz.jar edu

# Remove all class files
clean:
	rm -rf src/classes/edu
	rm -rf demo/hinote/src/classes/*.class
	rm -rf demo/graphit/src/classes/*.class
	rm -rf test/classes/*.class

hinote:
	java -classpath "src/classes:demo/hinote/src/classes:demo/hinote" HiNote

graphit:
	appletviewer demo/graphit/graphit.html

doc: docs

docs:
	javadoc -sourcepath src -use -doctitle "Jazz API Documentation" -windowtitle "Jazz API Documentation" -header "Jazz API Documentation" -footer "Jazz API Documentation" -d doc/api edu.umd.cs.jazz edu.umd.cs.jazz.component edu.umd.cs.jazz.event edu.umd.cs.jazz.io edu.umd.cs.jazz.util 

tags:
	etags src/edu/umd/cs/jazz/*/*.java src/edu/umd/cs/jazz/*/*/*.java
