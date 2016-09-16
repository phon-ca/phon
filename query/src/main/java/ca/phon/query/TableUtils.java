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
package ca.phon.query;

import java.text.ParseException;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.formatter.FormatterUtil;
import ca.phon.ipa.IPATranscript;

public class TableUtils {
	
	public static boolean checkEquals(Object o1, Object o2, boolean caseSensitive, boolean ignoreDiacritics) {
		if(o1 == null && o2 != null) return false;
		else if(o1 == null && o2 == o1) return true;
		
		final Class<?> type = o1.getClass();
		@SuppressWarnings("unchecked")
		final Formatter<Object> formatter = 
				(Formatter<Object>)FormatterFactory.createFormatter(type);
		
		String o1Txt = (formatter != null ? formatter.format(o1) : o1.toString());
		String o2Txt = (formatter != null ? formatter.format(o2) : o2.toString());
		
		if(ignoreDiacritics) {
			try {
				final IPATranscript ipa = IPATranscript.parseIPATranscript(o1Txt);
				o1Txt = ipa.removePunctuation().stripDiacritics().toString();
				
				final IPATranscript ipa2 = IPATranscript.parseIPATranscript(o2Txt);
				o2Txt = ipa2.removePunctuation().stripDiacritics().toString();
			} catch (ParseException e) {}
		}
		
		return (caseSensitive ? o1Txt.equals(o2Txt) : o1Txt.equalsIgnoreCase(o2Txt));
	}
	
	public static String objToString(Object val, boolean ignoreDiacritics) {
		String retVal = (val != null ? FormatterUtil.format(val) : "");
		if(ignoreDiacritics) {
			try {
				IPATranscript transcript = (val instanceof IPATranscript ? (IPATranscript) val :
					IPATranscript.parseIPATranscript(retVal));
				retVal = transcript.removePunctuation().stripDiacritics().toString();
			} catch (ParseException e) {
				
			}
		}
		return retVal;
	}

}
