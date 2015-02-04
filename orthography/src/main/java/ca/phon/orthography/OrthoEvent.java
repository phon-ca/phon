package ca.phon.orthography;

/**
 * An event written in-line with orthography.
 * 
 * Events can have syntax 'type:data' or just
 * 'data'.
 * 
 */
public class OrthoEvent extends AbstractOrthoElement {
	
	private final String type;
	
	private final String data;
	
	public OrthoEvent(String data) {
		this(null, data);
	}
	
	public OrthoEvent(String type, String data) {
		super();
		this.type = type;
		this.data = data;
	}
	
	/**
	 * Get the type of the event.  This is the text before
	 * the first ':' in the event text.
	 * 
	 * @return the type of the event.  Default is 'action' if
	 *  not defined
	 */
	public String getType() {
		return this.type;
	}
	
	/**
	 * Event data
	 * 
	 * @return the data for the event
	 */
	public String getData() {
		return this.data;
	}

	@Override
	public String text() {
		return ("*" + (this.type == null ? "" : this.type + ":") + this.data + "*");
	}
	
	@Override
	public String toString() {
		return text();
	}
	

}
