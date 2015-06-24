package parsers.query;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import parsers.Parser;
import query.QueryLexer;

public class QueryParser extends Parser {

	@Override
	public ParseTree parse(String content) {
		// normalize content
		content = content.trim();
		content = content.replaceAll("\\s+", " ");
		
		// Load the expression into the ANTLR stream.
		CharStream charStream = new ANTLRInputStream(content);
		
		// pass the input to the lexer to create tokens
		QueryLexer lexer = new QueryLexer(charStream);
		TokenStream tokens = new CommonTokenStream(lexer);
		
		// create a parser that feeds off the tokens buffer
		query.QueryParser parser = new query.QueryParser(tokens);
		
		// Begin parsing at init rule
		ParseTree tree = parser.init();

		// DEBUG
		//this.logger.log(tree.toStringTree(parser));

		return tree;
	}

}
