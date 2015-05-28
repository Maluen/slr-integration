package data;

public class Article {

	protected String source = "";
	protected String id = "";
	protected String title = "";
	protected String _abstract = "";
	protected String keywords = "";
	protected Integer year;
	protected String authors = "";
	protected String publication = "";
	
	public String getSource() {
		return this.source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getAuthors() {
		return this.authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

	public String getPublication() {
		return this.publication;
	}

	public void setPublication(String publication) {
		this.publication = publication;
	}
	
}
