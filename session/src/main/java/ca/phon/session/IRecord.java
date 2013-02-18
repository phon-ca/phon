/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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

import java.text.ParseException;
import java.util.List;


/**
 * An utterance.
 * 
 *
 */
public interface IRecord {
	
	/* 
	 * 2009-09-25
	 * New methods getTierString(String) and setTierString(String, String)
	 * have been created to help with editing records.  These methods are
	 * the only way to set data 'in-between' groups in the orthography
	 *  such as comments, events and chat codes.
	 */
	/**
	 * Get the tier data as a string.  Groups are enclosed by brackets
	 * as displayed in the UI.  This 
	 * 
	 * @param tierName
	 * @return the tier data as a string.
	 */
	public String getTierString(String tierName);
	
	/**
	 * Set the tier data from a given string.  This string is usually
	 * passed through some sort of parser to allow for correct encoding.
	 * 
	 * @param tierName
	 * @param data
	 * @throws ParserException when the string does not correctly
	 * pass the parsing process.
	 */
	public void setTierString(String tierName, String data)
		throws ParseException;
	
	/**
	 * The participant (speaker)
	 * 
	 * @return IParticipant
	 */
	public IParticipant getSpeaker();
	
	/**
	 * Set the participant
	 * 
	 * @paran IParticipant
	 */
	public void setSpeaker(IParticipant participant);
	
	/**
	 * Get media info
	 * 
	 * @return IMedia
	 */
	public IMedia getMedia();
	
	/**
	 * Set the meida
	 */
	public void setMedia(IMedia media);
	
	/**
	 * Get words
	 * 
	 * @return 
	 */
	public List<IWord> getWords();

	/**
	 * Create a new word and return the empty object.
	 * 
	 * @return IWord
	 */
	public IWord newWord();
	
	/**
	 * Create a new word at the given position and return the empty object.
	 * 
	 * @param pos
	 * @return IWord
	 */
	public IWord newWord(int pos);
	
	/**
	 * Create a new word group and return the empty object.
	 * 
	 * @return IWordGroup
	 */
	public IGroup newWordGroup();
	
	/**
	 * Create a new word group at the given position and return the empty object.
	 * 
	 * @param pos
	 * @return IWordGroup
	 */
	public IGroup newWordGroup(int pos);
	
	/**
	 * Remove a word.
	 * 
	 * @param work
	 */
	public void removeWord(IWord word);
	
	/**
	 * Remove the words at index i
	 * 
	 * @param index
	 */
	public void removeWord(int wordIndex);

	/**
	 * Get the notes
	 * 
	 * @return String
	 */
	public String getNotes();
	
	/**
	 * Set the notes
	 * 
	 * @param notes
	 */
	public void setNotes(String notes);
	
	/**
	 * Get the dependant tiers
	 * @return 
	 */
	public List<IDependentTier> getDependentTiers();
	
	/**
	 * Returns the list of word-aligned dependent tiers.
	 * @return ArrayList<String>
	 * @deprecated Phon 1.4
	 */
	@Deprecated
	public List<String> getWordAlignedTierNames();
	
	/**
	 * Create a new dependant tier and return the empty object.
	 * 
	 * @return IDependentTier
	 */
	public IDependentTier newDependentTier();
	
	/**
	 * Returns the dependent tier with the given name.
	 * 
	 * @return the dependent tier, or null if not found
	 */
	public IDependentTier getDependentTier(String tierName);
	
	/**
	 * Remove a dependent tier.
	 * 
	 * @param tier
	 */
	public void removeTier(IDependentTier tier);
	
	/**
	 * Get the unique id
	 * 
	 * @return String
	 */
	public String getID();
	
	/**
	 * Set the unique id
	 * 
	 * @param id
	 */
	public void setID(String id);
	
	/**
	 * Get the next unique phonetic rep id
	 * @return String
	 */
	public String getNextPhoRepID();
	
	/**
	 * Should we exclude this record from searches?
	 */
	public boolean isExcludeFromSearches();
	
	/**
	 * Set exclusion from searches.
	 */
	public void setExcludeFromSearches(boolean excluded);
}
