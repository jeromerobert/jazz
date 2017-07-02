# Makefile for jazz.util packange
#

JAZZ = ../../../../..
SOURCEPATH = $(JAZZ)
COMPILER = javac
JAVACC = javacc.bat
VM = java
COPTIONS = -g

# Install javaCC and run "javacc InternalParser.jj" to rebuild these files
JAVACC_GENERATED_FILES = ASCII_UCodeESC_CharStream.java \
	ZInternalParser.java \
	ZInternalParserConstants.java \
	ZInternalParserTokenManager.java \
	ParseException.java \
	Token.java \
	TokenMgrError.java 

CLASSES = ZExtendedInputStream.class \
	ZStreamPreprocessor.class \
	ZObjectOutputStream.class \
	ZSerializable.class 

PARSER_CLASSES = ASCII_UCodeESC_CharStream.class \
	ZParser.class \
	ZInternalParser.class \
	ZInternalParserConstants.class \
	ZInternalParserTokenManager.class \
	ParseException.class \
	Token.class \
	TokenMgrError.class 

all : $(JAVACC_GENERATED_FILES) $(CLASSES) $(PARSER_CLASSES)

# Remove all class files from the package directory.
clean:
	del *.class


# Implicit rule for making Java class files from Java
# source files.
%.class: %.java
	$(COMPILER) $(COPTIONS) -sourcepath "$(SOURCEPATH)"  $*.java

