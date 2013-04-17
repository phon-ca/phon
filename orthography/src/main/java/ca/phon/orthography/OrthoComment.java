package ca.phon.orthography;

/**
 * Comment with syntax
 *  type:data
 *  
 * type is optional
 */
public class OrthoComment implements OrthoElement {
	
	private final String type;
	
	private final String data;
	
	public OrthoComment(String data) {
		this(null, data);
	}
	
	public OrthoComment(String type, String data) {
		super();
		this.type = type;
		this.data = data;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String getData() {
		return this.data;
	}

	@Override
	public String text() {
		return ( (this.type == null ? "" : this.type + ":") + this.data );
	}

}
