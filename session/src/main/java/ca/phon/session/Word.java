/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.session;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.*;

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
	 * Return the IPAElement alignment for this aligned word pair.
	 * 
	 * @return phone alignment for IPA Target vs. IPA Actual
	 */
	public PhoneMap getPhoneAlignment();
	
	public int getPhoneAlignmentLocation();
	
	/**
	 * Notes - this will be the same for every group!
	 */
	public TierString getNotes();
	
	
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
