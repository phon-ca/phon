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
package ca.phon.query.script;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import ca.phon.application.transcript.IDepTierDesc;
import ca.phon.application.transcript.ITranscript;
import ca.phon.application.transcript.IUtterance;
import ca.phon.util.CollatorFactory;
import ca.phon.util.PhonDateFormat;

public class SSession {
	
	/** The transcript object */
	private ITranscript transcript;
	
	public SSession(ITranscript t) {
		super();
		
		this.transcript = t;
	}
	
	/**
	 * Return the session recording date in the
	 * format YYYY-MM-DD.
	 * 
	 * @return the session date
	 */
	public String getDate() {
		Calendar sessionDate = transcript.getDate();
		
		PhonDateFormat pdf = new PhonDateFormat(PhonDateFormat.YEAR_LONG);
		return pdf.format(sessionDate);
	}
	
	/**
	 * Get the number of records in the session.
	 * 
	 * @return the number of records
	 */
	public int getNumberOfRecords() {
		return transcript.getUtterances().size();
	}
	
	/**
	 * Get the record at the given index
	 * 
	 * @param index
	 * @return the record
	 */
	public SRecord getRecord(int index) {
		if(index < 0 || index >= getNumberOfRecords())
			return null;
		IUtterance utt = transcript.getUtterances().get(index);
		return new SRecord(index, utt, transcript);
	}
	
	/**
	 * Get the name of the session as <corpus>.<session>
	 * 
	 * @return the name of the corpus
	 */
	public String getName() {
		return transcript.getCorpus() + "." + transcript.getID();
	}
	
	/**
	 * Return an array containing all of the user-defined tiers
	 * in the session.
	 * 
	 * @return an arroy of String containing the user-defined tier
	 * names
	 */
	public String[] getTierNames() {
		List<String> retVal = new ArrayList<String>();
		for(IDepTierDesc tierDesc:transcript.getDependentTiers())
			retVal.add(tierDesc.getTierName());
		for(IDepTierDesc tierDesc:transcript.getWordAlignedTiers())
			retVal.add(tierDesc.getTierName());
		Collator collator = CollatorFactory.defaultCollator();
		Collections.sort(retVal, collator);
		return retVal.toArray(new String[0]);
	}
	
	/**
	 * Return the media location of the session
	 * 
	 * @return media location
	 */
	public String getMedia() {
		String retVal = transcript.getMediaLocation();
		
		return retVal;
	}
	
	/**
	 * Is the given tier name a group-aligned tier?
	 * 
	 * @param tierName
	 * @return <CODE>true</CODE> if the given tier name
	 * is a group tier, <CODE>false</CODE> otherwise.
	 */
	public boolean isGroupTier(String tierName) {
		boolean retVal = false;
		
		for(IDepTierDesc tierDesc:transcript.getWordAlignedTiers()) {
			if(tierDesc.getTierName().equals(tierName)) {
				retVal = true;
				break;
			}
		}
		
		return retVal;
	}
}
