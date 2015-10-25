package query;

public class ScidirectQueryOptimizerVisitor extends QueryOptimizerVisitor {

	@Override
	protected String optimizeAtomicTerm(String term) {
		return "(title("+term+") OR abstract("+term+") OR keywords("+term+"))";
	}

}
