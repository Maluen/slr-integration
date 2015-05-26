package data;

public class Article {

	protected String source = "";
	protected String title = "";
	protected String _abstract = "";
	protected String keywords = "";
	protected Integer year;
	
	public String getSource() {
		return this.source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTitle() {
		return this.title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getAbstract() {
		return this._abstract;
	}
	
	public void setAbstract(String _abstract) {
		this._abstract = _abstract;
	}
	
	public String getKeywords() {
		return this.keywords;
	}
	
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public Integer getYear() {
		return this.year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}
	
}
