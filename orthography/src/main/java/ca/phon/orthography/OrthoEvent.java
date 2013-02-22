package ca.phon.orthography;

/**
 * An event written in-line with orthography.
 * 
 * Events can have syntax 'type:data' or just
 * 'data'.
 * 
 */
public interface OrthoEvent {
	
	/**
	 * Get the type of the event.  This is the text before
	 * the first ':' in the event text.
	 * 
	 * @return the type of the event.  Default is 'action' if
	 *  not defined
	 */
	public String getType();
	
	/**
	 * Event data
	 * 
	 * @return the data for the event
	 */
	public String getData();

}
