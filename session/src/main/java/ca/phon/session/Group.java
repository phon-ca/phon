package ca.phon.session;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;

/**
 * A Group is a vertial view of tier information
 * in a record.
 * 
 */
public interface Group {
	
	/**
	 * Get orthography
	 */
	public Orthography getOrthography();
	
	/**
	 * Set orthography
	 * 
	 * @param ortho
	 */
	public void setOrthography(Orthography ortho);

	/**
	 * IPA target
	 */
	public IPATranscript getIPATarget();
	
	/**
	 * IPA actual
	 */
	public IPATranscript getIPAActual();
	
	/**
	 * Get the value for the specfieid tier
	 * and type.
	 * 
	 * @param name
	 * @param type
	 * 
	 * @return the value for the specified tier or
	 *  <code>null</code> if not found
	 *  
	 */
	public <T> T getTier(String name, Class<T> type);
	
	/**
	 * Get the value for the specified tier
	 * as a String
	 * 
	 * @param name
	 * 
	 * @return value for the tier as text or
	 *  <code>null</code> if not found
	 */
	public String getTier(String name);
}
