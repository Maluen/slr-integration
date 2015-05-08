package parsers.language;

import language.ExprLexer;
import language.ExprParser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import parsers.Parser;

public class LanguageParser extends Parser {

	@Override
	public ParseTree parse(String content) {
		// Load the expression into the ANTLR stream.
		CharStream charStream = new ANTLRInputStream(content);
		
		// pass the input to the lexer to create tokens
		ExprLexer lexer = new ExprLexer(charStream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		
		// create a parser that feeds off the tokens buffer
		ExprParser parser = new ExprParser(tokens);

		// Begin parsing at init rule
		ParseTree tree = parser.init();

		return tree;
	}

}
