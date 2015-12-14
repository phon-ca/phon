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
package ca.phon.ipadictionary.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.phon.ipadictionary.ContractionRule;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.exceptions.IPADictionaryExecption;
import ca.phon.ipadictionary.spi.AddEntry;
import ca.phon.ipadictionary.spi.GenerateSuggestions;
import ca.phon.ipadictionary.spi.IPADictionarySPI;
import ca.phon.ipadictionary.spi.LanguageInfo;
import ca.phon.ipadictionary.spi.NameInfo;
import ca.phon.ipadictionary.spi.OrthoKeyIterator;
import ca.phon.ipadictionary.spi.PrefixSearch;
import ca.phon.ipadictionary.spi.RemoveEntry;
import ca.phon.util.Language;
import ca.phon.util.Tuple;

/**
 * Perform lookups on multiple dictionaries at once.
 * 
 */
public class CompoundDictionary implements IPADictionarySPI,
	NameInfo, LanguageInfo, PrefixSearch {
	
	/**
	 * Dictionaries
	 * 
	 */
	private final IPADictionary[] dicts;

	public CompoundDictionary(IPADictionary[] dicts) {
		this.dicts = dicts;
	}
	
	@Override
	public String[] lookup(String orthography) throws IPADictionaryExecption {
		Set<String> allTranscripts = new LinkedHashSet<String>();
		
		if(orthography.contains("+")) {
			return lookupCompound(orthography, "\\+", true);
		} else if(orthography.contains("~")) {
			return lookupCompound(orthography, "~", true);
		} else if(orthography.contains("-")) {
			return lookupCompound(orthography, "-", false);
		}
		
		for(IPADictionary dict:dicts) {
			for(String s:dict.lookup(orthography)) {
				allTranscripts.add(s);
			}
		}
		
		return allTranscripts.toArray(new String[0]);
	}

	private String[] lookupCompound(String orthography, String charRegex, boolean includeSeparator) {
		// deal with contractions
		String regex = "(.+)" + charRegex + "(.+)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(orthography);
		String[] retVal = new String[0];
		if(m.matches()) {
			String lhs = m.group(1);
			String rhs = m.group(2);
			
			// get entries for both sides
			String[] lhsEntries = new String[0];
			try {
				lhsEntries = lookup(lhs);
			} catch (IPADictionaryExecption e) {
				
			}
			
			String[] rhsEntries = new String[0];
			try {
				rhsEntries = lookup(rhs);
			} catch (IPADictionaryExecption e ) {
				
			}
			
			Set<String> transcriptions = new HashSet<String>();
			
			final List<Tuple<String, String>> ipaPairs = new ArrayList<Tuple<String,String>>();
			for(String lhsEntry:lhsEntries) {
				if(rhsEntries.length == 0) {
					final Tuple<String, String> ipaPair = new Tuple<String, String>(lhsEntry, new String());
					ipaPairs.add(ipaPair);
				} else {
					for(String rhsEntry:rhsEntries) {
						final Tuple<String, String> ipaPair = new Tuple<String, String>(lhsEntry, rhsEntry);
						ipaPairs.add(ipaPair);
					}
				}
			}
			
			for(Tuple<String, String> ipaPair:ipaPairs) {
				final String lhsEntry = ipaPair.getObj1();
				final String rhsEntry = ipaPair.getObj2();
				
				final StringBuilder sb = new StringBuilder();
				sb.append(lhsEntry);
				if(includeSeparator)
					sb.append(charRegex.charAt(charRegex.length()-1));
				sb.append(rhsEntry);
				
				transcriptions.add(sb.toString());
			}
			retVal = transcriptions.toArray(new String[0]);
		}
		return retVal;
	}
	
	@Override
	public String getName() {
		return "Compound";
	}

	@Override
	public Language getLanguage() {
		Language detectedLang = new Language();
		if(dicts.length > 0) {
			detectedLang = dicts[0].getLanguage();
			
			for(int i = 1; i < dicts.length; i++) {
				if(!dicts[i].getLanguage().equals(detectedLang)) {
					detectedLang.appendUserID(dicts[i].getLanguage().toString());
				}
			}
		}
		
		return detectedLang;
	}
	
	@Override
	public String[] keysWithPrefix(String prefix) {
		Set<String> keys = new TreeSet<String>();
		
		for(IPADictionary dict:dicts) {
			try {
				for(String key:dict.prefixSearch(prefix)) {
					keys.add(key);
				}
			} catch (IPADictionaryExecption e) {
				e.printStackTrace();
			}
		}
		
		return keys.toArray(new String[0]);
	}

	@Override
	public void install(IPADictionary dict) {
		for(IPADictionary d:dicts) {
			final AddEntry addEntry = d.getExtension(AddEntry.class);
			if(addEntry != null) {
				dict.putExtension(AddEntry.class, addEntry);
			}
			final RemoveEntry removeEntry = d.getExtension(RemoveEntry.class);
			if(removeEntry != null) {
				dict.putExtension(RemoveEntry.class, removeEntry);
			}
//			final GenerateSuggestions genSuggestions = d.getExtension(GenerateSuggestions.class);
//			if(genSuggestions != null) {
//				dict.putExtension(GenerateSuggestions.class, genSuggestions);
//			}
			final OrthoKeyIterator orthoItr = d.getExtension(OrthoKeyIterator.class);
			if(orthoItr != null) {
				dict.putExtension(OrthoKeyIterator.class, orthoItr);
			}
		}
		
		// override some extensions
		dict.putExtension(NameInfo.class, this);
		dict.putExtension(LanguageInfo.class, this);
		dict.putExtension(PrefixSearch.class, this);
	}
	
}
