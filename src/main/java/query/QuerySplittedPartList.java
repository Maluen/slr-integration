package query;

import java.util.ArrayList;

public class QuerySplittedPartList extends ArrayList<QuerySplittedPart> {

	private static final long serialVersionUID = -6653854545362458600L;

	public static enum Type {
		AND,
		OR
	}
	
	protected Type type;
	
	public QuerySplittedPartList(Type type) {
		this.setType(type);
	}

	public Type getType() {
		return this.type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public Float getScore() {
		Float score = 0f;
		
		for (QuerySplittedPart querySplittedPart : this) {
			score += querySplittedPart.getScore();
		}
		
		return score;
	}
	
}
