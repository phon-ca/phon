package ca.phon.app.session.editor.view.find_and_replace;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.formatter.FormatterUtil;
import ca.phon.ipa.IPATranscript;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;
import ca.phon.util.Range;

public class FindExpr {
	
	public enum SearchType {
		PLAIN("Plain text"),
		REGEX("Regular expression"),
		PHONEX("Phonex");
		
		private String title;
		
		private SearchType(String title) {
			this.title = title;
		}
		
		@Override
		public String toString() {
			return this.title;
		}
	}
	
	private SearchType type;
	
	private boolean caseSensitive;
	
	private String expr;
	
	/* Regex members */
	private Pattern regexPattern;
	
	private Matcher lastMatcher;
	
	/* Phonex members */
	private PhonexPattern phonexPattern;
	
	private PhonexMatcher lastPhonexMatcher;
	
	public FindExpr() {
		this(SearchType.PLAIN, "", true);
	}
	
	public FindExpr(String expr) {
		this(SearchType.PLAIN, expr, true);
	}
	
	public FindExpr(String expr, boolean caseSensitive) {
		this(SearchType.PLAIN, expr, caseSensitive);
	}
	
	public FindExpr(SearchType type, String expr, boolean caseSensitive) {
		super();
		this.type = type;
		this.expr = expr;
		this.caseSensitive = caseSensitive;
	}

	public SearchType getType() {
		return type;
	}

	public void setType(SearchType type) {
		this.type = type;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public String getExpr() {
		return expr;
	}

	public void setExpr(String expr) {
		this.expr = expr;
	}
	
	public Range findNext(Object obj, int charIdx) {
		Range retVal = null;
		
		if(getType() == SearchType.PLAIN) {
			retVal = findNextPlain(FormatterUtil.format(obj), charIdx);
		} else if(getType() == SearchType.REGEX) {
			retVal = findNextRegex(FormatterUtil.format(obj), charIdx);
		} else if(getType() == SearchType.PHONEX) {
			if(obj instanceof IPATranscript) {
				retVal = findNextPhonex((IPATranscript)obj, charIdx);
			} 
		}
		
		return retVal;
	}
	
	public Range findPrev(Object obj, int charIdx) {
		Range retVal = null;
		
		if(getType() == SearchType.PLAIN) {
			retVal = findPrevPlain(FormatterUtil.format(obj), charIdx);
		} else if(getType() == SearchType.REGEX) {
			retVal = findPrevRegex(FormatterUtil.format(obj), charIdx);
		} else if(getType() == SearchType.PHONEX) {
			if(obj instanceof IPATranscript) {
				retVal = findPrevPhonex((IPATranscript)obj, charIdx);
			}
		}
		
		return retVal;
	}
	
	public Range findNextPlain(String txt, int charIdx) {
		final String expr = (isCaseSensitive() ? getExpr() : getExpr().toLowerCase());
		final String val = (isCaseSensitive() ? txt : txt.toLowerCase());
		int nextIdx = val.indexOf(expr, charIdx);
		if(nextIdx >= 0) {
			return new Range(nextIdx, nextIdx + expr.length(), false);
		}
		return null;
	}
	
	public Range findPrevPlain(String txt, int charIdx) {
		final String expr = (isCaseSensitive() ? getExpr() : getExpr().toLowerCase());
		final String val = (isCaseSensitive() ? txt.substring(0, charIdx) : txt.substring(0, charIdx).toLowerCase());
		
		int prevIdx = val.lastIndexOf(expr);
		if(prevIdx >= 0) {
			return new Range(prevIdx, prevIdx + expr.length(), false);
		}
		return null;
	}
	
	public Range findNextRegex(String txt, int charIdx) {
		if(regexPattern == null) {
			regexPattern = Pattern.compile(getExpr(), (isCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE));
		}
		lastMatcher = regexPattern.matcher(txt);
		
		if(lastMatcher.find(charIdx)) {
			return new Range(lastMatcher.start(), lastMatcher.end(), false);
		}
		return null;
	}
	
	public Range findPrevRegex(String txt, int charIdx) {
		if(regexPattern == null) {
			regexPattern = Pattern.compile(getExpr(), (isCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE));
		}
		lastMatcher = regexPattern.matcher(txt);
		
		int start = -1;
		int end = -1;
		while(lastMatcher.find()) {
			start = lastMatcher.start();
			end = lastMatcher.end();
		}
		
		if(start >= 0 && end >= start) {
			return new Range(start, end, false);
		}
		return null;
	}
	
	public Range findNextPhonex(IPATranscript ipa, int charIdx) {
		if(phonexPattern == null) {
			phonexPattern = PhonexPattern.compile(getExpr());
		}
		lastPhonexMatcher = phonexPattern.matcher(ipa);
		
		// convert charIdx to ipa idx
		final int idx = ipa.ipaIndexOf(charIdx);
		if(idx >= 0) {
			if(lastPhonexMatcher.find(idx)) {
				final int ipaStart = lastPhonexMatcher.start();
				final int ipaEnd = lastPhonexMatcher.end();
				
				final int start = ipa.stringIndexOfElement(ipaStart);
				final int end = ipa.stringIndexOfElement(ipaEnd);
				
				return new Range(start, end, false);
			}
		}
		return null;
	}
	
	public Range findPrevPhonex(IPATranscript ipa, int charIdx) {
		if(phonexPattern == null) {
			phonexPattern = PhonexPattern.compile(getExpr());
		}
		int lastPhonexIdx = ipa.ipaIndexOf(charIdx);
		if(lastPhonexIdx < 0 && charIdx == ipa.toString().length()) lastPhonexIdx = ipa.length();
		
		final IPATranscript val = ipa.subsection(0, lastPhonexIdx);
		lastPhonexMatcher = phonexPattern.matcher(val);
		
		int ipaStart = -1;
		int ipaEnd = -1;
		while(lastPhonexMatcher.find()) {
			ipaStart = lastPhonexMatcher.start();
			ipaEnd = lastPhonexMatcher.end();
		}
		
		if(ipaStart >= 0 && ipaEnd >= ipaStart) {
			final int start = ipa.stringIndexOfElement(ipaStart);
			final int end = ipa.stringIndexOfElement(ipaEnd);
			
			if(start >= 0 && end >= start) {
				return new Range(start, end, false);
			}
		}
		
		return null;
	}
	
}
