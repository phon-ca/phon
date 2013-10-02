 package ca.phon.session;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.Orthography;

/**
 * A Group is a vertical view of tier information
 * in a record.
 * 
 */
public interface Group {
	
	/**
	 * Record
	 */
	public Record getRecord();
	
	/**
	 * Group index
	 */
	public int getGroupIndex();
	
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
	
	public void setIPATarget(IPATranscript ipa);
	
	/**
	 * IPA actual
	 */
	public IPATranscript getIPAActual();
	
	public void setIPAActual(IPATranscript ipa);
	
	/**
	 * Alignment
	 */
	public PhoneMap getPhoneAlignment();
	
	public void setPhoneAlignment(PhoneMap alignment);
	
	/**
	 * Notes - this will be the same for every group!
	 */
	public String getNotes();
	
	public void setNotes(String notes);
	
	/**
	 * Get the value for the specified tier
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
	 * Get the tier.
	 * @param name
	 */
	public Object getTier(String name);
	
	public <T> void setTier(String name, Class<T> type, T val);
	
}
