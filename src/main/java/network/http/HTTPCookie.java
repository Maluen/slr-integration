package network.http;

public class HTTPCookie {

	private String name = "";
	private String value = "";
	private String expires = "";
	private String path = "";
	private String domain = "";
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getExpires() {
		return this.expires;
	}
	
	public void setExpires(String expires) {
		this.expires = expires;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getDomain() {
		return this.domain;
	}
	
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	
	
	
}
