// Generated from Query.g4 by ANTLR 4.5
package query;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link QueryParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface QueryVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link QueryParser#init}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInit(QueryParser.InitContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Not}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNot(QueryParser.NotContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Parenthesis}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthesis(QueryParser.ParenthesisContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Strict}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStrict(QueryParser.StrictContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Word}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWord(QueryParser.WordContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Or}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr(QueryParser.OrContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Wildcard}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWildcard(QueryParser.WildcardContext ctx);
	/**
	 * Visit a parse tree produced by the {@code PhraseCase}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPhraseCase(QueryParser.PhraseCaseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code And}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnd(QueryParser.AndContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Epsilon}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEpsilon(QueryParser.EpsilonContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SpacesLeft}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpacesLeft(QueryParser.SpacesLeftContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SpacesRight}
	 * labeled alternative in {@link QueryParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpacesRight(QueryParser.SpacesRightContext ctx);
	/**
	 * Visit a parse tree produced by {@link QueryParser#phrase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPhrase(QueryParser.PhraseContext ctx);
}