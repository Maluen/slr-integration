// Generated from Expr.g4 by ANTLR 4.5
package language;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ExprParser}.
 */
public interface ExprListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ExprParser#init}.
	 * @param ctx the parse tree
	 */
	void enterInit(ExprParser.InitContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExprParser#init}.
	 * @param ctx the parse tree
	 */
	void exitInit(ExprParser.InitContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExprParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(ExprParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExprParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(ExprParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExprParser#phrase}.
	 * @param ctx the parse tree
	 */
	void enterPhrase(ExprParser.PhraseContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExprParser#phrase}.
	 * @param ctx the parse tree
	 */
	void exitPhrase(ExprParser.PhraseContext ctx);
}