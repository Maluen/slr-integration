package converters.document.from;

import services.Data;
import converters.document.DocumentConverter;

public abstract class FromDocument extends DocumentConverter {

	protected Data<String> data;
	
	public abstract String getToContentType();
	
	public Data<String> getData() {
		return this.data;
	}

	public void setData(Data<String> data) {
		this.data = data;
	}

}
