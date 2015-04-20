package converters.document.to;


public abstract class TextToDocument extends ToDocument {
	
	protected String content;
	
	@Override
	public String getContent() {
		return this.content;
	}

	@Override
	public void setContent(Object content) {
		this.content = (String) content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}

}
