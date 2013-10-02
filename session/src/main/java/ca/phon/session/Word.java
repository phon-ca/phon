package ca.phon.session;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.OrthoElement;

/**
 * <p>Aligned word access for a record. A word is typically any text content
 * between spaces unless otherwise specified by the specific tier
 * implementation.  The class allows for vertical access of
 * a word in each tier.</p>
 * 
 * <p>Words can only be obtained through {@link Group} objects.<br/>
 * <pre>
 * Group g = record.getGroup(0);
 * Word w = g.getWord(1);
 * 
 * // these 'word' elements are aligned
 * OrthoElement ele = record.getOrthography();
 * IPATranscript ipaA = record.getIPAActual();
 * 
 * </pre>
 * </p>
 * 
 * 
 */
public interface Word {

	/**
	 * Get the group this word belongs to
	 *
	 * @return the parent group
	 */
	public Group getGroup();
	
	/**
	 * Get the word index
	 * 
	 * @return the word index
	 */
	public int getWordIndex();
	
	/**
	 * Get orthography
	 * 
	 * 
	 * @return the {@link OrthoElement} for this aligned word index.
	 */
	public OrthoElement getOrthography();
	
	/**
	 * Return the start index of the aligned word in the
	 * string representation of the parent group's {@link Orthography}.
	 * 
	 * @return the start index for this aligned word index or -1 if
	 *  not found
	 */
	public int getOrthographyWordLocation();


	/**
	 * IPA target
	 * 
	 * @return the model {@link IPATranscript} for the aligned word index
	 */
	public IPATranscript getIPATarget();
	
	/**
	 * Return the start index of this aligned word in the
	 * string representation of the parent group's model {@link IPATranscript}
	 * 
	 * @return the start index for this aligned word index or -1 if
	 *  not found
	 */
	public int getIPATargetWordLocation();
	
	/**
	 * IPA actual
	 * 
	 * @return the actual {@link IPATranscript} for the aligned word index
	 */
	public IPATranscript getIPAActual();
	
	/**
	 * Return the start index of this aligned word in the
	 * string representation of the parent group's actual {@link IPATranscript}
	 * 
	 * @return the start index for this aligned word index or -1 if
	 *  not found
	 */
	public int getIPAActualWordLocation();
	
	/**
	 * Notes - this will be the same for every group!
	 */
	public String getNotes();
	
	
	/**
	 * Return the start index of this aligned word in the
	 * string representation of the parent group's Notes tier
	 * 
	 * @return the start index for this aligned word index or -1
	 *  if not found
	 */
	public int getNotesWordLocation();
	
	/**
	 * Get the tier.
	 * @param name
	 */
	public Object getTier(String name);
	
	/**
	 * Return the start index of this aligned word in the
	 * string representation of the specified tier
	 * 
	 * @param tierName
	 * 
	 * @return the start index for this aligned word index or -1
	 *  if not found
	 */
	public int getTierWordLocation(String tierName);
	
}
