package query;

import java.util.Arrays;
import java.util.List;

import misc.Logger;
import misc.Utils;

import org.apache.commons.lang3.StringUtils;

public class QueryMatcherVisitor extends QueryBaseVisitor<Boolean> {
	
	Logger logger;
	
	private String target;
	private List<String> targetPartList;
	
	public QueryMatcherVisitor() {
		this.logger = new Logger("QueryMatcherVisitor");
	}
	
	public QueryMatcherVisitor(String target) {
		this();
		
		this.setTarget(target);
	}
	
	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
		
		this.targetPartList = this.normalizePhrase(target);
	}
	
	public List<String> normalizePhrase(String phrase) {
		return Arrays.asList(
			Utils.simplify(phrase).split("\\s+") 
		);
	}
	
	public Boolean doTargetHasPhrase(String phrase) {
		List<String> phrasePartList = this.normalizePhrase(phrase);
		
		// (adding spaces at the beginning allows to do a strict search)
		String phraseSearch = " " + StringUtils.join(phrasePartList, " ") + " ";
		// (adding spaces at the beginning allows to find phrase if it touches borders)
		String targetSearch = " " + StringUtils.join(this.targetPartList, " ") + " ";
		
		return targetSearch.indexOf(phraseSearch) != -1;
	}
	
	@Override
	public Boolean visitNot(QueryParser.NotContext ctx) {
		this.logger.log("Visiting Not: " + ctx.getText());
		return this.visit(ctx.expr()) == false;
	}

	@Override
	public Boolean visitAnd(QueryParser.AndContext ctx) {
		this.logger.log("Visiting And: " + ctx.getText());
		return this.visit(ctx.expr(0)) && this.visit(ctx.expr(1));
	}
	
	@Override
	public Boolean visitOr(QueryParser.OrContext ctx) {
		this.logger.log("Visiting Or: " + ctx.getText());
		return this.visit(ctx.expr(0)) || this.visit(ctx.expr(1));
	}
	
	@Override
	public Boolean visitParenthesis(QueryParser.ParenthesisContext ctx) {
		this.logger.log("Visiting Parenthesis: " + ctx.getText());
		return this.visit(ctx.expr());
	}
	
	@Override
	public Boolean visitWildcard(QueryParser.WildcardContext ctx) {
		this.logger.log("Visiting Wildcard: " + ctx.getText());
		
		String wordStart = ctx.WORD().getText();
		
		for (String targetPart : targetPartList) {
			if (targetPart.startsWith(wordStart)) return true;
		}
		
		return false;
	}
	
	@Override
	public Boolean visitStrict(QueryParser.StrictContext ctx) {
		this.logger.log("Visiting Strict: " + ctx.getText());

		String phrase = ctx.phrase().getText();
		
		return this.doTargetHasPhrase(phrase);
	}
	
	@Override
	public Boolean visitWord(QueryParser.WordContext ctx) {
		this.logger.log("Visiting Word: " + ctx.getText());
		
		String word = ctx.WORD().getText();
		
		return this.targetPartList.contains(word);
	}
	
	/*
	@Override
	public Boolean visitSpaces(QueryParser.SpacesContext ctx) {
		this.logger.log("Visiting Spaces: " + ctx.getText());
		// like an AND
		return this.visit(ctx.expr(0)) && this.visit(ctx.expr(1));
	}
	*/
	
	@Override
	public Boolean visitSpacesLeft(QueryParser.SpacesLeftContext ctx) {
		this.logger.log("Visiting Spaces Left: " + ctx.getText());
		return this.visit(ctx.expr());
	}
	
	@Override
	public Boolean visitSpacesRight(QueryParser.SpacesRightContext ctx) {
		this.logger.log("Visiting Spaces Right: " + ctx.getText());
		return this.visit(ctx.expr());
	}
	
	/*
	public Boolean visitSpacesBoth(QueryParser.SpacesBothContext ctx) {
		this.logger.log("Visiting Spaces Both: " + ctx.getText());
		return this.visit(ctx.expr());
	}
	*/
	
	public Boolean visitPhraseCase(QueryParser.PhraseCaseContext ctx) {
		this.logger.log("Visiting Phrase Case: " + ctx.getText());

		String phrase = ctx.phrase().getText();
		
		// true if all the words of the phrase appears in the target,
		// regardless of their position
		List<String> phrasePartList = this.normalizePhrase(phrase);
		return this.targetPartList.containsAll(phrasePartList);
	}
	
	@Override
	public Boolean visitEpsilon(QueryParser.EpsilonContext ctx) {
		this.logger.log("Visiting Epsilon: " + ctx.getText());
		return true;
	}
	
}
