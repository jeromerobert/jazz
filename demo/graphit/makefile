#
# This makefile works with GNU make to build HiNote
#

# Compile all the java source code
all :
	cd src; javac -g -d classes -classpath ../../../src/classes *.java
	cd src/classes; jar -cf ../../graphit.jar *.class
	jar -uf graphit.jar resources/*.gif
	cd ../../src/classes; jar -uf ../../demo/graphit/graphit.jar edu

# Remove all class files
clean:
	rm -rf src/classes/*.class

rungraphit:
	appletviewer graphit.html
