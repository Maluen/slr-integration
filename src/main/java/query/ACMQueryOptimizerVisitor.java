// Optimize by only searching in the fields we are interested in		

package query;

import org.apache.commons.lang3.StringUtils;

public class ACMQueryOptimizerVisitor extends QueryOptimizerVisitor {

	@Override
	protected String optimizeAtomicTerm(String term) {
		return "(Title:"+term + " OR Abstract:"+term + " OR Keywords:"+term + ")";
	}
	
}
