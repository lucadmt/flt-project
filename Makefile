Lexer: st169656/analyzers/Lexer.java
	javac st169656/analyzers/Lexer.java
	java st169656.analyzers.Lexer

Parser: st169656/analyzers/Parser.java
	javac st169656/analyzers/Parser.java
	java st169656.analyzers.Parser

Evaluator: st169656/analyzers/Evaluator.java
	javac st169656/analyzers/Evaluator.java
	java st169656.analyzers.Evaluator

ExpressionTranslator: st169656/analyzers/ExpressionTranslator.java
	javac st169656/analyzers/ExpressionTranslator.java
	java st169656.analyzers.ExpressionTranslator
	java -jar jasmin.jar Output.j

Translator: st169656/analyzers/Translator.java
	javac st169656/analyzers/Translator.java
	java st169656.analyzers.Translator
	java -jar jasmin.jar Output.j

run: Output.j
	java Output

clean:
	find . -type f -name '*.class' -delete
	find . -type f -name 'Output.j' -delete