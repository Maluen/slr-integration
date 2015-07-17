// Optimize by only searching in the fields we are interested in		

package query;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;

public class ACMQueryOptimizerVisitor extends QueryBaseVisitor<String> {

	protected String optimizeAtomicTerm(String term) {
		return "(Title:"+term + " OR Abstract:"+term + " OR Keywords:"+term + ")";
	}
	
	@Override
	public String visitNot(QueryParser.NotContext ctx) {
		return "NOT" + this.visit(ctx.expr());
	}
	
	@Override
	public String visitAnd(QueryParser.AndContext ctx) {
		return this.visit(ctx.expr(0)) + "AND" + this.visit(ctx.expr(1));
	}
	
	@Override
	public String visitOr(QueryParser.OrContext ctx) {
		return this.visit(ctx.expr(0)) + "OR" + this.visit(ctx.expr(1));
	}
	
	@Override
	public String visitParenthesis(QueryParser.ParenthesisContext ctx) {
		return "(" + this.visit(ctx.expr()) + ")";
	}
	
	@Override
	public String visitWildcard(QueryParser.WildcardContext ctx) {
		return this.optimizeAtomicTerm(ctx.getText());
	}
	
	@Override
	public String visitStrict(QueryParser.StrictContext ctx) {
		return this.optimizeAtomicTerm(ctx.getText());
	}
	
	@Override
	public String visitWord(QueryParser.WordContext ctx) {
		return this.processWordToken(ctx.WORD());
		
	}
	
	public String processWordToken(TerminalNode wordToken) {
		return this.optimizeAtomicTerm(wordToken.getText());
	}
	
	@Override
	public String visitSpacesLeft(QueryParser.SpacesLeftContext ctx) {
		return ctx.WS().getText() + this.visit(ctx.expr());
	}
	
	@Override
	public String visitSpacesRight(QueryParser.SpacesRightContext ctx) {
		return this.visit(ctx.expr()) + ctx.WS().getText();
	}
	
	public String visitPhraseCase(QueryParser.PhraseCaseContext ctx) {
		return this.visit( ctx.phrase() );
	}
	
	public String visitPhrase(QueryParser.PhraseContext ctx) {
		String[] wordList = ctx.getText().trim().split("\\s+");
		
		String[] optimizedWordExpressionList = new String[wordList.length];
		for (int i=0; i<wordList.length; i++) {
			// word -> Title:word OR Abstract:word OR Keywords:word (parenthesis are removed)
			optimizedWordExpressionList[i] = this.optimizeAtomicTerm(wordList[i]).replaceAll("[()]", "");
		}
		
		String optimizedPhrase = "(" + StringUtils.join(optimizedWordExpressionList, " OR ") + ")";
		return optimizedPhrase;
	}
	
	@Override
	public String visitEpsilon(QueryParser.EpsilonContext ctx) {
		return "";
	}
	
	
	
	
	
}
