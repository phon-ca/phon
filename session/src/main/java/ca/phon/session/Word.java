/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.session;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.ipa.alignment.SyllableMap;
import ca.phon.orthography.OrthoElement;
import ca.phon.orthography.Orthography;

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
	
	public SyllableMap getSyllableAlignment();
	
	public int getSyllableAlignmentLocation();
	
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
	
	/**
	 * Return the number of aligned syllables in this
	 * group.
	 * 
	 * @return number of aligned syllables
	 */
	public int getAlignedSyllableCount();
	
	/**
	 * Return the aligned syllable at given index
	 * 
	 * @param index
	 * 
	 * @return aligned syllable
	 */
	public AlignedSyllable getAlignedSyllable(int index);
	
}
