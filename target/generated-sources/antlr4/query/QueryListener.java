// Generated from Query.g4 by ANTLR 4.5
package query;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link QueryParser}.
 */
public interface QueryListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link QueryParser#init}.
	 * @param ctx the parse tree
	 */
	void enterInit(QueryParser.InitContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#init}.
	 * @param ctx the parse tree
	 */
	void exitInit(QueryParser.InitContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SpacesRight}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterSpacesRight(QueryParser.SpacesRightContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SpacesRight}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitSpacesRight(QueryParser.SpacesRightContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Word}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterWord(QueryParser.WordContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Word}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitWord(QueryParser.WordContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SpacesLeft}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterSpacesLeft(QueryParser.SpacesLeftContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SpacesLeft}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitSpacesLeft(QueryParser.SpacesLeftContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Or}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterOr(QueryParser.OrContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Or}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitOr(QueryParser.OrContext ctx);
	/**
	 * Enter a parse tree produced by the {@code PhraseCase}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterPhraseCase(QueryParser.PhraseCaseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code PhraseCase}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitPhraseCase(QueryParser.PhraseCaseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Wildcard}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterWildcard(QueryParser.WildcardContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Wildcard}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitWildcard(QueryParser.WildcardContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Epsilon}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterEpsilon(QueryParser.EpsilonContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Epsilon}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitEpsilon(QueryParser.EpsilonContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Parenthesis}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterParenthesis(QueryParser.ParenthesisContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Parenthesis}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitParenthesis(QueryParser.ParenthesisContext ctx);
	/**
	 * Enter a parse tree produced by the {@code And}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAnd(QueryParser.AndContext ctx);
	/**
	 * Exit a parse tree produced by the {@code And}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAnd(QueryParser.AndContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Strict}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterStrict(QueryParser.StrictContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Strict}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitStrict(QueryParser.StrictContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Not}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterNot(QueryParser.NotContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Not}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitNot(QueryParser.NotContext ctx);
	/**
	 * Enter a parse tree produced by {@link QueryParser#phrase}.
	 * @param ctx the parse tree
	 */
	void enterPhrase(QueryParser.PhraseContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#phrase}.
	 * @param ctx the parse tree
	 */
	void exitPhrase(QueryParser.PhraseContext ctx);
}