package ca.phon.app.opgraph.nodes.query;

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
