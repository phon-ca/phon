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
package ca.phon.query;

import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.formatter.FormatterUtil;
import ca.phon.ipa.Diacritic;
import ca.phon.ipa.IPATranscript;

public class TableUtils {
	
	public static boolean checkEquals(Object o1, Object o2, boolean caseSensitive, boolean ignoreDiacritics) {
		return checkEquals(o1, o2, caseSensitive, ignoreDiacritics, false, new HashSet<>());
	}
	
	public static boolean checkEquals(Object o1, Object o2, boolean caseSensitive, boolean ignoreDiacritics, boolean onlyOrExcept, Collection<Diacritic> selectedDiacritics) {
		if(o1 == null && o2 != null) return false;
		else if(o1 == null && o2 == o1) return true;
		
		final Class<?> type = o1.getClass();
		@SuppressWarnings("unchecked")
		final Formatter<Object> formatter = 
				(Formatter<Object>)FormatterFactory.createFormatter(type);
		
		String o1Txt = (formatter != null ? formatter.format(o1) : o1.toString());
		String o2Txt = (formatter != null ? formatter.format(o2) : o2.toString());
		
		if(ignoreDiacritics && o1 instanceof IPATranscript && o2 instanceof IPATranscript) {
			try {
				final IPATranscript ipa = IPATranscript.parseIPATranscript(o1Txt);
				o1Txt = objToString(ipa, ignoreDiacritics, onlyOrExcept, selectedDiacritics);
				
				final IPATranscript ipa2 = IPATranscript.parseIPATranscript(o2Txt);
				o2Txt =  objToString(ipa2, ignoreDiacritics, onlyOrExcept, selectedDiacritics);
			} catch (ParseException e) {}
		}
		
		return (caseSensitive ? o1Txt.equals(o2Txt) : o1Txt.equalsIgnoreCase(o2Txt));
	}
	
	@SuppressWarnings("unchecked")
	public static int compare(Object o1, Object o2, boolean caseSensitive, boolean ignoreDiacritics, boolean onlyOrExcept, Collection<Diacritic> selectedDiacritics) {
		if(o1 == null && o2 != null) return 1;
		else if(o1 == null && o2 == o1) return 0;
		
		Formatter<Object> formatter = null;
		if(o1.getClass() == o2.getClass()) {
			final Class<?> type = o1.getClass();
			formatter = (Formatter<Object>)FormatterFactory.createFormatter(type);
		}
		
		String o1Txt = (formatter != null ? formatter.format(o1) : o1.toString());
		String o2Txt = (formatter != null ? formatter.format(o2) : o2.toString());
		
		if(ignoreDiacritics && o1 instanceof IPATranscript && o2 instanceof IPATranscript) {
			try {
				final IPATranscript ipa = IPATranscript.parseIPATranscript(o1Txt);
				o1Txt = objToString(ipa, ignoreDiacritics, onlyOrExcept, selectedDiacritics);
				
				final IPATranscript ipa2 = IPATranscript.parseIPATranscript(o2Txt);
				o2Txt =  objToString(ipa2, ignoreDiacritics, onlyOrExcept, selectedDiacritics);
			} catch (ParseException e) {}
		}
		
		return o1Txt.compareTo(o2Txt);
	}
	
	public static String objToString(Object val, boolean ignoreDiacritics) {
		return objToString(val, ignoreDiacritics, false, new HashSet<>());
	}
	
	public static String objToString(Object val, boolean ignoreDiacritics, boolean onlyOrExcept, Collection<Diacritic> selectedDiacritics) {
		String retVal = (val != null ? FormatterUtil.format(val) : "");
		if(ignoreDiacritics && val instanceof IPATranscript) {
			try {
				IPATranscript transcript = (val instanceof IPATranscript ? (IPATranscript) val :
					IPATranscript.parseIPATranscript(retVal));
				retVal =
						(onlyOrExcept
								? transcript.removePunctuation().stripDiacritics(selectedDiacritics).toString()
								: transcript.removePunctuation().stripDiacriticsExcept(selectedDiacritics)).toString();
			} catch (ParseException e) {
				
			}
		}
		return retVal;
	}

}
