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
	
	public SyllableMap getSyllableAlignment();
	
	/**
	 * Notes - this will be the same for every group!
	 */
	public TierString getNotes();
	
	public void setNotes(TierString notes);
	
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
	
	/**
	 * Get the aligned word data for the given index
	 * 
	 * @param wordIndex
	 * 
	 * @return the aligned word data for the given index
	 */
	public Word getAlignedWord(int wordIndex);
	
	/**
	 * Return the number of aligned words based on
	 * the orthography tier.
	 * 
	 * @return the number of aligned words in orthography
	 */
	public int getAlignedWordCount();
	
	/**
	 * Get the word count for the specified tier
	 * 
	 * @param tierName
	 * @return the number of words in the tier, or 0 if the tier
	 *  is empty or not a grouped tier
	 */
	public int getWordCount(String tierName);
	
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
