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
package ca.phon.syllable;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.phon.ipa.phone.Phone;

/**
 * The syllable entity object.
 *
 */
public class Syllable {
	
	public final static char PrimaryStressChar = 0x02c8; 
	public final static char SecondaryStressChar = 0x02cc;
	
	/** The list of phones in this syllable */
	private Phone[] phones;
	
	/** Constructor */
	public Syllable() {
		this(new Phone[0]);
	}
	
	public Syllable(Phone[] phones) {
		super();
		
		this.phones = phones;
	}
	
	/**
	 * Get stress.
	 * We can't set stress, that must be
	 * done from within the phone list.
	 * 
	 * @return SyllableStress
	 */
	public SyllableStress getStress() {
		if(this.phones.length == 0)
			return SyllableStress.NoStress;
		
		// stress is determined by the initial phone
		String stressString = this.phones[0].getText();
		
		if(stressString.length() == 0) 
			return SyllableStress.NoStress;
		
//		if(stressString.equals(PrimaryStressChar))
//			return SyllableStress.Primary;
//		else if(stressString.equals(SecondaryStressChar))
//			return SyllableStress.Secondary;
//		else
//			return SyllableStress.Unknown;
	
		if(stressString.charAt(0) == PrimaryStressChar)
			return SyllableStress.PrimaryStress;
		else if(stressString.charAt(0) == SecondaryStressChar)
			return SyllableStress.SecondaryStress;
		else
			return SyllableStress.NoStress;
	}
	
	/**
	 * Returns the type of syllable this is.
	 * (e.g., CCV, CVCV, etc.)
	 * @return String
	 */
	public String getSyllableType() {
		String retVal = new String();
		
		for(Phone p:phones) {
			if(p.getFeatureSet().hasFeature("Consonant"))
				if(p.getFeatureSet().hasFeature("Glide"))
					retVal += "G";
				else
					retVal += "C";
			else if(p.getFeatureSet().hasFeature("Vowel"))
				retVal += "V";
			else if(p.getScType() == SyllableConstituentType.SYLLABLEBOUNDARYMARKER
					|| p.getScType() == SyllableConstituentType.SYLLABLESTRESSMARKER)
				retVal += "";
			else
				retVal += "_";
		}
		
		return retVal;
	}
	
	/**
	 * Is this a real syllable?  We don't count
	 * syllables of <CODE>SyllableConstituentType.Unknown</CODE>.
	 * 
	 * @return boolean <CODE>true</CODE> if none of the
	 *  phones are Unknown and there is at least one phone
	 */
	public boolean isRealSyllable() {
		if(this.phones.length == 0)
			return false;
		
		for(Phone p:this.phones)
			if(p.getScType() == SyllableConstituentType.UNKNOWN)
				return false;
		
		return true;
	}

	public Phone[] getPhones() {
		return phones;
	}

	public void setPhones(Phone[] phones) {
		this.phones = phones;
	}
	
	/**
	 * Returns a list of phones given an identifier.
	 * 
	 * @param identifier the identifier of the phones to retrieve
	 * if no phone is available for the given identifier, null
	 * is return in the list.  The available identifiers are<br/>
	 * <ul>
	 *  <li>LA - Left Appendix</li>
	 *  <li>O - Onset</li>
	 *  <li>N - Nucleus</li>
	 *  <li>C - Coda</li>
	 *  <li>RA - Right Appendix</li>
	 *  <li>OEHS - OEHS</li>
	 * </ul>
	 * <br/>
	 * Each position can have an optional numerical paramter which
	 * allows for the i'th phone for that identifier.  (e.g., O2 is the
	 * second phone in the onset.
	 * 
	 * @return ArrayList<Phone>
	 */
	public ArrayList<Phone> getPhonesForIdentifier(String identifier) {
		ArrayList<Phone> retVal = new ArrayList<Phone>();
		
		// the identifier regex
		String idRegex = new String("([A-Z]{1,4})([0-9]{0,2})?");
		Pattern idPattern = Pattern.compile(idRegex);
		Matcher matcher = idPattern.matcher(identifier);
		
		if(matcher.matches()) {
			String id = matcher.group(1);
			SyllableConstituentType scType = SyllableConstituentType.getTypeForIdentifier(id);
			
			if(scType != null) {
				ArrayList<Phone> phones = 
					getPhonesForSyllableConstituent(scType);
				
				// check to see if we are asking for a particular phone
				String pos = matcher.group(2);
				
				if(pos != null && pos.length() > 0) {
					int numPhone = Integer.parseInt(pos);
					
					if(numPhone-1 >= phones.size())
						retVal.add(null);
					else
						retVal.add(phones.get(numPhone-1));
				} else {
					retVal.addAll(phones);
				}
				
				if(retVal.size() == 0)
					retVal.add(null);
				
				if(scType == SyllableConstituentType.SYLLABLESTRESSMARKER
						&& retVal.size() == 0)
					retVal.add(new Phone(" "));
			}
		}
		
		return retVal;
	}
	
	/**
	 * Returns the list of phones for a specified syllable
	 * constituent.
	 * 
	 * @param scType
	 * @return ArrayList<Phone>
	 */
	public ArrayList<Phone> getPhonesForSyllableConstituent(SyllableConstituentType scType) {
		Phone[] phones = getPhones();
		
		ArrayList<Phone> retVal = new ArrayList<Phone>();
		
		for(int i = 0; i < phones.length; i++) {
			Phone p = phones[i];
			
			if(p.getScType() == scType)
				retVal.add(p);
			
			if(scType == SyllableConstituentType.ONSET
					&& p.getScType() == SyllableConstituentType.AMBISYLLABIC
					&& i == 0)
				retVal.add(p);
			
			if(scType == SyllableConstituentType.CODA
					&& p.getScType() == SyllableConstituentType.AMBISYLLABIC
					&& i == phones.length-1)
				retVal.add(p);
		}
		
		if(scType == SyllableConstituentType.SYLLABLESTRESSMARKER
				&& retVal.size() == 0)
			retVal.add(new Phone(" "));
		
		return retVal;
	}
	
	@Override
	public String toString() {
		String retVal = new String();
		
		for(Phone p:phones) {
			retVal += p.getPhoneString();
		}
		
		return retVal;
	}
}

