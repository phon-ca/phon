/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
	
}
