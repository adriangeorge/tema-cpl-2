# Set the path to the ANTLR jar here:
ANTLR=/usr/local/lib/antlr4.jar
# This line might get overwritten by the vmchecker.

#
# Main rules:
#
# * build - Compiles everything
# * test - Runs the automated tester
# * clean - Deletes binary files and test-output files 
# * clean-all - Does everything the "clean" rule does. Then removes the ANTLR auto-generated files.
#

.PHONY: build run run-lexer test zip clean clean-all

build: bin/cool/compiler/Compiler.class bin/cool/tester/Tester2.class bin/cool/tester/Tester1.class

test: bin/cool/compiler/Compiler.class bin/cool/tester/Tester2.class
	java -cp 'bin:$(ANTLR)' cool.tester.Tester2

test_old: bin/cool/compiler/Compiler.class bin/cool/tester/Tester1.class
	java -cp 'bin:$(ANTLR)' cool.tester.Tester1

clean:
	rm -rf ./bin
	rm -f ./tests/tema2/*.out

clean-all: clean
	rm -f cool/lexer/CoolLexer.interp cool/lexer/CoolLexer.java cool/lexer/CoolLexer.tokens
	rm -f cool/parser/CoolParser.interp cool/parser/CoolParser.tokens cool/parser/CoolParser*.java

#
# Helper rules:
#
# * zip - generates a vmchecker-friendly archive
#
# * run - runs a specific test
#   Should be used like this:
#       make ARGS=tests/tema2/01-define-class.cl run
#
# * run-lexer - runs the lexer on a specific test
#   Should be used like this:
#       make ARGS=tests/tema2/01-define-class.cl run-lexer
#

zip:
	make clean-all
	make build
	make clean
	rm -f archive.zip
	zip -r archive.zip . --exclude "tests/*" --exclude ".*"
#
# Do not change the "run" rule unless you have to!
# It's used by the Tester class to launch the compiler.
#
run: bin/cool/compiler/Compiler.class
	java -cp 'bin:$(ANTLR)' cool.compiler.Compiler $(ARGS)

run-lexer: bin/cool/compiler/Compiler.class
	java -cp 'bin:$(ANTLR)' org.antlr.v4.gui.TestRig cool.lexer.Cool tokens -tokens $(ARGS)

#
# Build tasks:
#

# Generate the lexer code.
cool/lexer/CoolLexer.java: cool/lexer/CoolLexer.g4
	java -jar $(ANTLR) cool/lexer/CoolLexer.g4

# Generate the parser code. It depends on the lexer code.
cool/parser/CoolParser.java: cool/parser/CoolParser.g4 cool/lexer/CoolLexer.java bin/cool/compiler/ASTNode.class
	java -jar $(ANTLR) -lib cool/lexer -visitor -listener cool/parser/CoolParser.g4

# Compile other classes
bin/cool/compiler/ASTNode.class: cool/compiler/ASTNode.java
	javac -g -cp '.:$(ANTLR)' -d bin cool/compiler/ASTNode.java

bin/cool/compiler/ASTVisitor.class: cool/compiler/ASTNode.java
	javac -g -cp '.:$(ANTLR)' -d bin cool/compiler/ASTVisitor.java

bin/cool/compiler/PrintPassVisitor.class: bin/cool/compiler/ASTVisitor.class
	javac -g -cp '.:$(ANTLR)' -d bin cool/compiler/PrintPassVisitor.java

bin/cool/compiler/CoolParserVisitor.class: bin/cool/compiler/PrintPassVisitor.class
	javac -g -cp '.:$(ANTLR)' -d bin cool/compiler/CoolParserVisitor.java

bin/cool/compiler/DefinitionPassVisitor.class: bin/cool/compiler/PrintPassVisitor.class
	javac -g -cp '.:$(ANTLR)' -d bin cool/compiler/DefinitionPassVisitor.java
	
bin/cool/compiler/ResolutionPassVisitor.class: bin/cool/compiler/PrintPassVisitor.class
	javac -g -cp '.:$(ANTLR)' -d bin cool/compiler/ResolutionPassVisitor.java

# Compile the compiler. It depends on the parser code.
bin/cool/compiler/Compiler.class: cool/compiler/Compiler.java cool/parser/CoolParser.java
	javac -g -cp '.:$(ANTLR)' -d bin cool/compiler/Compiler.java


# Compile the tester.
bin/cool/tester/Tester2.class: cool/tester/Tester2.java
	javac -g -cp '.:$(ANTLR)' -d bin cool/tester/Tester2.java

bin/cool/tester/Tester1.class: cool/tester/Tester1.java
	javac -g -cp '.:$(ANTLR)' -d bin cool/tester/Tester1.java

