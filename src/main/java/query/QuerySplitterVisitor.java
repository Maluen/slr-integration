package query;

import org.antlr.v4.runtime.tree.TerminalNode;

public class QuerySplitterVisitor extends QueryBaseVisitor<QuerySplittedPartList> {

	public static enum TargetType {
		WORD,
		WILDCARD
	}
	
	protected final Float WORD_SCORE = 1f;
	protected final Float WILDCARD_SCORE = 3f;
	
	protected TargetType target;
	protected Integer targetMaxCount;
	
	public QuerySplittedPartList removeDuplicates(QuerySplittedPartList exprList) {
		QuerySplittedPartList groupedExprList = new QuerySplittedPartList( QuerySplittedPartList.Type.AND );
		groupedExprList.addAll(exprList); // clone
		
		for (int i=0; i<groupedExprList.size(); i++) {
			QuerySplittedPart exprI = groupedExprList.get(i);
			for (int j=i+1; j<groupedExprList.size(); j++) {
				QuerySplittedPart exprJ = groupedExprList.get(j);
				
				if (exprI.getQueryText().equals(exprJ.getQueryText())) {
					// duplicate
					groupedExprList.remove(j);
					// continue checking from the new element at position j (j==j-1+1 on the next iteration)
					j = j-1;
				}
			}
		}
		
		return groupedExprList;
	}
	
	// Group elements of the AND-list when possible.
	// Note: assumes that each element, taken alone, respects the target constraints.
	// Return a new OR-list containing only the largest element (after the grouping).
	public QuerySplittedPartList groupAnd(QuerySplittedPartList exprList) {
		QuerySplittedPartList groupedExprList = new QuerySplittedPartList( QuerySplittedPartList.Type.AND );
		groupedExprList.addAll(exprList); // clone
		
		for (int i=0; i<groupedExprList.size(); i++) {
			QuerySplittedPart exprI = groupedExprList.get(i);
			for (int j=i+1; j<groupedExprList.size(); j++) {
				QuerySplittedPart exprJ = groupedExprList.get(j);
				
				Integer joinedTargetCount = exprI.getTargetCount() + exprJ.getTargetCount();
				
				if (exprI.getQueryText().equals(exprJ.getQueryText())) {
					// remove duplicate
					groupedExprList.remove(j);
					// continue checking from the new element at position j (j==j-1+1 on the next iteration)
					j = j-1;
					
				} else if (joinedTargetCount <= this.targetMaxCount) {
					// exprI and exprJ can be joined together
					// => (exprI) AND (exprJ)
					String joinedQueryText = "(" 
												+ exprI.getQueryText()
												+ " AND "
												+ exprJ.getQueryText() 
										   + ")";
					
					// In the worst case, all the results of the "smaller" parameter
					// will be returned, but it is very rare and we want to give
					// precedence to (min AND other) with respect to min, thus the minus epsilon
					Float joinedScore = Math.min(exprI.getScore(), exprJ.getScore()) - 0.00001f;
					if (joinedScore < 0) {
						// the min is 0
						joinedScore = 0f;
					}
					
					QuerySplittedPart joinedExpr = new QuerySplittedPart();
					joinedExpr.setQueryText(joinedQueryText);
					joinedExpr.setTargetCount(joinedTargetCount);
					joinedExpr.setScore(joinedScore);
					
					// replace exprI with the joined element, remove exprJ
					groupedExprList.set(i, joinedExpr);
					groupedExprList.remove(j);
					// continue checking from the new element at position j (j==j-1+1 on the next iteration)
					j = j-1;
				}
			}
		}
		
		// only keep the expression with lowest score, since is the one most likely
		// to return fewer results
		QuerySplittedPart min = null;
		Float minLength = Float.MAX_VALUE;
		for (int i=0; i<groupedExprList.size(); i++) {
			QuerySplittedPart exprI = groupedExprList.get(i);
			Float exprIScore = exprI.getScore();
			if (exprIScore <= minLength) {
				min = exprI;
				minLength = exprIScore;
			}
		}
		groupedExprList = new QuerySplittedPartList( QuerySplittedPartList.Type.OR );
		if (min != null) {
			groupedExprList.add(min);
		}
		
		return groupedExprList;
	}
	
	// Group elements of the OR-list when possible and removes duplicates.
	// Note: assumes that each element, taken alone, respects the target constraints.
	public QuerySplittedPartList groupOr(QuerySplittedPartList exprList) {
		QuerySplittedPartList groupedExprList = new QuerySplittedPartList( QuerySplittedPartList.Type.OR );
		groupedExprList.addAll(exprList); // clone
		
		for (int i=0; i<groupedExprList.size(); i++) {
			QuerySplittedPart exprI = groupedExprList.get(i);
			for (int j=i+1; j<groupedExprList.size(); j++) {
				QuerySplittedPart exprJ = groupedExprList.get(j);
				
				Integer joinedTargetCount = exprI.getTargetCount() + exprJ.getTargetCount();
				
				if (exprI.getQueryText().equals(exprJ.getQueryText())) {
					// remove duplicate
					groupedExprList.remove(j);
					// continue checking from the new element at position j (j==j-1+1 on the next iteration)
					j = j-1;
					
				} else if (joinedTargetCount <= this.targetMaxCount) {
					// exprI and exprJ can be joined together
					// => (exprI) OR (exprJ)
					String joinedQueryText = "(" 
												+ exprI.getQueryText()
												+ " OR "
												+ exprJ.getQueryText() 
										   + ")";
					
					Float joinedScore = exprI.getScore() + exprJ.getScore();
					
					QuerySplittedPart joinedExpr = new QuerySplittedPart();
					joinedExpr.setQueryText(joinedQueryText);
					joinedExpr.setTargetCount(joinedTargetCount);
					joinedExpr.setScore(joinedScore);
					
					// replace exprI with the joined element, remove exprJ
					groupedExprList.set(i, joinedExpr);
					groupedExprList.remove(j);
					// continue checking from the new element at position j (j==j-1+1 on the next iteration)
					j = j-1;
				}
			}
		}
		
		// can't do any cutting since it would decrease the search space
		
		return groupedExprList;
	}
	
	@Override
	// NOT expr
	public QuerySplittedPartList visitNot(QueryParser.NotContext ctx) {
		// exprList: expr1, expr2, ..., exprM
		QuerySplittedPartList exprList = this.visit( ctx.expr() );
		
		// notExprList: (NOT expr1), (NOT expr2), ..., (NOT exprM)
		QuerySplittedPartList notList = new QuerySplittedPartList( exprList.getType() );
		for (QuerySplittedPart exprI : exprList) {
			String notQueryText =  "(NOT "+exprI.getQueryText()+")";
			Integer notTargetCount = exprI.getTargetCount();
			
			// The NOT returns the WHOLE results minus those of the parameter
			Float notScore = Float.MAX_VALUE - exprI.getScore();
			
			QuerySplittedPart not = new QuerySplittedPart();
			not.setQueryText(notQueryText);
			not.setTargetCount(notTargetCount);
			not.setScore(notScore);
			
			notList.add(not);
		}
		
		return notList;
	}
	
	@Override
	// exprA AND exprB
	public QuerySplittedPartList visitAnd(QueryParser.AndContext ctx) {
		QuerySplittedPartList exprAList = this.visit( ctx.expr(0) );
		QuerySplittedPartList exprBList = this.visit( ctx.expr(1) );
		
		// Calculated list:
		// (exprA1 OR ... OR exprAN) AND (exprB1 OR ... OR exprBN)
		// -> ( (exprA1 AND exprB1) OR ... OR (exprA1 AND exprBN) ) OR ... OR ( (exprAN AND exprB1) OR ... OR (exprAN AND exprBN) )
		QuerySplittedPartList calculatedExprList = new QuerySplittedPartList( QuerySplittedPartList.Type.OR );
		if (exprAList.isEmpty()) {
			calculatedExprList = exprBList;
		} else if (exprBList.isEmpty()) {
			calculatedExprList = exprAList;
		} else {
			for (int i=0; i<exprAList.size(); i++) {
				QuerySplittedPart exprAI = exprAList.get(i);
				for (int j=0; j<exprBList.size(); j++) {
					QuerySplittedPart exprBJ = exprBList.get(j);
					
					QuerySplittedPartList exprAIAndExprBJList = new QuerySplittedPartList( QuerySplittedPartList.Type.AND );
					exprAIAndExprBJList.add(exprAI);
					exprAIAndExprBJList.add(exprBJ);
					exprAIAndExprBJList = this.groupAnd(exprAIAndExprBJList);
					
					// add the one element returned by the AND-grouping
					calculatedExprList.addAll(exprAIAndExprBJList);
				}
			}
			// remove duplicates
			calculatedExprList = this.groupOr(calculatedExprList);
		}
		
		// Keep the best list between the first, the second and the calculated list
		QuerySplittedPartList bestList;
		if (exprAList.getScore() <= exprBList.getScore()) {
			bestList = (exprAList.getScore() <= calculatedExprList.getScore()) ? exprAList : calculatedExprList;
		} else {
			bestList = (exprBList.getScore() <= calculatedExprList.getScore()) ? exprBList : calculatedExprList;
		}
		
		return bestList;
	}
	
	@Override
	// exprA OR exprB
	public QuerySplittedPartList visitOr(QueryParser.OrContext ctx) {
		QuerySplittedPartList exprAList = this.visit( ctx.expr(0) );
		QuerySplittedPartList exprBList = this.visit( ctx.expr(1) );
		
		// the two lists are both OR-lists
		QuerySplittedPartList orList = new QuerySplittedPartList( QuerySplittedPartList.Type.OR );
		orList.addAll(exprAList);
		orList.addAll(exprBList);
		
		return this.groupOr(orList);
	}
	
	@Override
	// (expr)
	public QuerySplittedPartList visitParenthesis(QueryParser.ParenthesisContext ctx) {
		QuerySplittedPartList exprList = this.visit( ctx.expr() );
		
		// parExprList: (expr1), (expr2), ..., (exprM)
		QuerySplittedPartList parenthesisList = new QuerySplittedPartList( exprList.getType() );
		for (QuerySplittedPart exprI : exprList) {
			String parenthesisQueryText =  "("+exprI.getQueryText()+")";
			Integer parenthesisTargetCount = exprI.getTargetCount();
			Float parenthesisScore = exprI.getScore();
			
			QuerySplittedPart parenthesis = new QuerySplittedPart();
			parenthesis.setQueryText(parenthesisQueryText);
			parenthesis.setTargetCount(parenthesisTargetCount);
			parenthesis.setScore(parenthesisScore);
			
			parenthesisList.add(parenthesis);
		}
		
		return parenthesisList;
	}
	
	@Override
	// WORD*
	public QuerySplittedPartList visitWildcard(QueryParser.WildcardContext ctx) {		
		QuerySplittedPartList wildcardList = new QuerySplittedPartList( QuerySplittedPartList.Type.OR );
		
		String queryText = ctx.getText();
		Integer targetCount = (this.target == TargetType.WILDCARD || this.target == TargetType.WORD) ? 1 : 0;
		
		QuerySplittedPart wildcard = new QuerySplittedPart();
		wildcard.setQueryText(queryText);
		wildcard.setTargetCount(targetCount);
		wildcard.setScore(this.WILDCARD_SCORE);
		
		wildcardList.add(wildcard);
		return wildcardList;
	}
	
	@Override
	// "phrase"
	public QuerySplittedPartList visitStrict(QueryParser.StrictContext ctx) {
		QuerySplittedPartList phraseList = this.visit( ctx.phrase() );
		
		// parExprList: "phrase1", "phrase2", ..., "phraseM"
		QuerySplittedPartList strictList = new QuerySplittedPartList( phraseList.getType() );
		for (QuerySplittedPart phraseI : phraseList) {
			String strictQueryText =  "\""+phraseI.getQueryText()+"\"";
			Integer strictTargetCount = phraseI.getTargetCount();
			
			// Strict means searching adjacent words, thus the score
			// starts at the word score (if only 1 word is used)
			// and decreases proportionally to the number of words used.
			Float strictScore;
			Integer phraseWordsCount = phraseI.getQueryText().split("\\s+").length;
			if (phraseWordsCount != 0) { // avoid division by 0
				strictScore = this.WORD_SCORE / phraseWordsCount;
			} else {
				strictScore = 0f;
			}
			
			QuerySplittedPart strict = new QuerySplittedPart();
			strict.setQueryText(strictQueryText);
			strict.setTargetCount(strictTargetCount);
			strict.setScore(strictScore);
			
			strictList.add(strict);
		}
		
		return strictList;
	}
	
	@Override
	// WORD
	public QuerySplittedPartList visitWord(QueryParser.WordContext ctx) {
		return this.processWordToken(ctx.WORD());
	}
	
	public QuerySplittedPartList processWordToken(TerminalNode wordToken) {
		QuerySplittedPartList wordList = new QuerySplittedPartList( QuerySplittedPartList.Type.OR );
		
		String queryText = wordToken.getText();
		Integer targetCount = (this.target == TargetType.WORD) ? 1 : 0;
		
		QuerySplittedPart word = new QuerySplittedPart();
		word.setQueryText(queryText);
		word.setTargetCount(targetCount);
		word.setScore(this.WORD_SCORE);
		
		wordList.add(word);
		return wordList;
	}
	
	@Override
	// WS expr
	public QuerySplittedPartList visitSpacesLeft(QueryParser.SpacesLeftContext ctx) {
		QuerySplittedPartList exprList = this.visit( ctx.expr() );
		return exprList;
	}
	
	@Override
	// expr WS
	public QuerySplittedPartList visitSpacesRight(QueryParser.SpacesRightContext ctx) {
		QuerySplittedPartList exprList = this.visit( ctx.expr() );
		return exprList;
	}
	
	@Override
	// phrase
	public QuerySplittedPartList visitPhraseCase(QueryParser.PhraseCaseContext ctx) {
		return this.visit( ctx.phrase() );
	}
	
	@Override
	// WORD (WS PHRASE)?
	public QuerySplittedPartList visitPhrase(QueryParser.PhraseContext ctx) {
		QuerySplittedPartList wordList = this.processWordToken( ctx.WORD() );
		
		// phrase is optional
		QuerySplittedPartList innerPhraseList;
		if (ctx.phrase() != null) {
			innerPhraseList = this.visit( ctx.phrase() );
		} else {
			innerPhraseList = new QuerySplittedPartList( QuerySplittedPartList.Type.AND ); // empty
		}
			
	    QuerySplittedPartList phraseList = new QuerySplittedPartList( QuerySplittedPartList.Type.AND );
	    phraseList.addAll(wordList);
	    phraseList.addAll(innerPhraseList);
	    
	    // will basically convert list of "WORD WORD WORD" 
	    // into smaller (one-element) list of "((WORD AND WORD) AND WORD)"
	    // (where the order of the words is preserved)
	    phraseList = this.groupAnd(phraseList);
	    
	    // remove the AND and the parenthesis, since the phrase might be inside
	    // a strict expression or similar, thus requiring a real phrase
	    for (int i=0; i<phraseList.size(); i++) {
	    	QuerySplittedPart phrase = phraseList.get(i);
	    	
	    	String phraseText = phrase.getQueryText();
	    	phraseText = phraseText.replaceAll(" AND ", " ")
	    						   .replaceAll("[\\(|\\)]", ""); // parenthesis
	    	
	    	phrase.setQueryText(phraseText);
	    }
	    
	    return phraseList;
	}
	
	@Override
	public QuerySplittedPartList visitEpsilon(QueryParser.EpsilonContext ctx) {
		/*QuerySplittedPartList epsilonList = new QuerySplittedPartList();
		
		// empty expr
		QuerySplittedPart epsilon = new QuerySplittedPart();
		epsilon.setQueryText("");
		epsilon.setTargetCount(0);
		
		epsilonList.add(epsilon);
		return epsilonList;*/
		
		return new QuerySplittedPartList( QuerySplittedPartList.Type.OR ); // empty
	}
	
	public TargetType getTarget() {
		return this.target;
	}

	public void setTarget(TargetType target) {
		this.target = target;
	}

	public Integer getTargetMaxCount() {
		return this.targetMaxCount;
	}

	public void setTargetMaxCount(Integer targetMaxCount) {
		this.targetMaxCount = targetMaxCount;
	}
	
}
