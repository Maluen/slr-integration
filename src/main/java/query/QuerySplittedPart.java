package query;

public class QuerySplittedPart {
	protected String queryText;
	protected Integer targetCount;
	protected Float score;
	
	public String getQueryText() {
		return this.queryText;
	}
	
	public void setQueryText(String queryText) {
		this.queryText = queryText;
	}
	
	public Integer getTargetCount() {
		return this.targetCount;
	}
	
	public void setTargetCount(Integer targetCount) {
		this.targetCount = targetCount;
	}
	
	public Float getScore() {
		return this.score;
	}

	public void setScore(Float score) {
		this.score = score;
	}
	
}
