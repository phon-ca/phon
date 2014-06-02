package ca.phon.app.session.editor.view.find_and_replace;

import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.phon.formatter.FormatterUtil;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;
import ca.phon.util.Range;

public class FindExpr {
	
	private static final Logger LOGGER = Logger.getLogger(FindExpr.class
			.getName());
	
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
	
	/* Last matched object */
	private Object lastObj;
	
	/* Plain members */
	private Range lastRange;
	
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
		
		if(retVal != null) {
			lastObj = obj;
		} else {
			lastObj = null;
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
		
		if(retVal != null) {
			lastObj = obj;
		} else {
			lastObj = null;
		}
		
		return retVal;
	}
	
	public Range findNextPlain(String txt, int charIdx) {
		final String expr = (isCaseSensitive() ? getExpr() : getExpr().toLowerCase());
		final String val = (isCaseSensitive() ? txt : txt.toLowerCase());
		int nextIdx = val.indexOf(expr, charIdx);
		if(nextIdx >= 0) {
			lastRange = new Range(nextIdx, nextIdx + expr.length(), false);
			return lastRange;
		} else {
			lastRange = null;
		}
		return null;
	}
	
	public Range findPrevPlain(String txt, int charIdx) {
		final String expr = (isCaseSensitive() ? getExpr() : getExpr().toLowerCase());
		final String val = (isCaseSensitive() ? txt.substring(0, charIdx) : txt.substring(0, charIdx).toLowerCase());
		
		int prevIdx = val.lastIndexOf(expr);
		if(prevIdx >= 0) {
			lastRange = new Range(prevIdx, prevIdx + expr.length(), false);
			return lastRange;
		} else {
			lastRange = null;
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
		lastMatcher.region(0, charIdx);
		
		int start = -1;
		int end = -1;
		while(lastMatcher.find()) {
			start = lastMatcher.start();
			end = lastMatcher.end();
		}
		
		if(start >= 0 && end >= start) {
			// reset matcher to position
			lastMatcher.find(start);
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
		
		lastPhonexMatcher = phonexPattern.matcher(ipa);
		lastPhonexMatcher.region(0, lastPhonexIdx);
		
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
				lastPhonexMatcher.find(start);
				return new Range(start, end, false);
			}
		}
		
		return null;
	}

	public Object replace(String expr) {
		Object retVal = lastObj;
		
		if(lastObj != null) {
			if(type == SearchType.PLAIN) {
				return replacePlain(lastObj, expr);
			} else if(type == SearchType.REGEX) {
				return replaceRegex(lastObj, expr);
			} else if(type == SearchType.PHONEX) {
				if(retVal instanceof IPATranscript) {
					try {
						return replacePhonex((IPATranscript)retVal, IPATranscript.parseIPATranscript(expr));
					} catch (ParseException e) {
						LOGGER.log(Level.SEVERE,
								e.getLocalizedMessage(), e);
					}
				}
			}
		}
		
		return retVal;
	}
	
	public Object replacePlain(Object obj, String expr) {
		final String val = FormatterUtil.format(obj);
		
		final StringBuffer buffer = new StringBuffer();
		if(lastRange != null) {
			buffer.append(val.substring(0, lastRange.getStart()));
			buffer.append(expr);
			buffer.append(val.substring(lastRange.getEnd()));
		}
		
		final String newTxt = buffer.toString();
		return FormatterUtil.parse(obj.getClass(), newTxt);
	}
	
	public Object replaceRegex(Object obj, String expr) {
		final StringBuffer buffer = new StringBuffer();
		if(lastMatcher != null) {
			lastMatcher.appendReplacement(buffer, expr);
			lastMatcher.appendTail(buffer);
		}
		
		final String newTxt = buffer.toString();
		return FormatterUtil.parse(obj.getClass(), newTxt);
	}
	
	public IPATranscript replacePhonex(IPATranscript ipa, IPATranscript expr) {
		final IPATranscriptBuilder builder = new IPATranscriptBuilder();
		if(lastPhonexMatcher != null) {
			lastPhonexMatcher.appendReplacement(builder, expr);
			lastPhonexMatcher.appendTail(builder);
		}
		return builder.toIPATranscript();
	}
	
}
