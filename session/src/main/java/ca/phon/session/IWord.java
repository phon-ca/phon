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

import java.util.List;

import ca.phon.alignment.PhoneMap;

public interface IWord {
	
	/**
	 * Get the word value as a string.
	 * @return String
	 */
	public String getWord();
	
	/**
	 * Set the word value.
	 * @param word
	 */
	public void setWord(String word);
	
	/**
	 * Get the phonetic representations of the word.
	 * @return ArrayList&lt;IPhoneticRep&gt;
	 */
	public List<IPhoneticRep> getPhoneticRepresentations();
	
	/**
	 * Get the phonetic rep identified by the given form.
	 * @return IPhoneticRep
	 */
	public IPhoneticRep getPhoneticRepresentation(Form form);

	/**
	 * Add a new phonetic rep and return the empty object.
	 * @return IPhoneticRep
	 */
	public IPhoneticRep newPhoneticRepresentation();
	
	/**
	 * Remove a phonetic rep.
	 * @param phoRep
	 */
	public void removePhoneticRepresentation(IPhoneticRep rep);
	
	/**
	 * Get the phone alignment 
	 * @return AlignmentMap&lt;Phone&gt;
	 */
	public PhoneMap getPhoneAlignment();
	
	/**
	 * Set the phone alignment
	 * @param phoneMap
	 */
	public void setPhoneAlignment(PhoneMap phoneMap);
	
	/**
	 * Get the dependant tiers
	 * @return 
	 */
	public List<IDependentTier> getDependentTiers();
	
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
	 * Get a blind transcription.
	 */
	public String getBlindTranscription(Form form, ITranscriber user);
	public void setBlindTranscription(Form form, ITranscriber user, String transcription);
	
//	/**
//	 * Get the syllable alignment
//	 * @return AlignmentMap&lt;Syllable&gt;
//	 */
//	public SyllableMap getSyllableAlignment();
//	
//	/**
//	 * Set the syllable alignment
//	 * @param syllMap
//	 */
//	public void setSyllableAlignment(SyllableMap syllMap);
}
