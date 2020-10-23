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
package ca.phon.app.session.editor.search;

import java.text.*;
import java.util.regex.*;

import ca.phon.extensions.*;
import ca.phon.formatter.*;
import ca.phon.ipa.*;
import ca.phon.phonex.*;
import ca.phon.util.*;

public class FindExpr {
	
	private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(FindExpr.class
			.getName());
	
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
		
		String plainTxt = FormatterUtil.format(obj);
		if(plainTxt.length() == 0 && obj instanceof IExtendable) {
			final UnvalidatedValue uv = ((IExtendable)obj).getExtension(UnvalidatedValue.class);
			if(uv != null) {
				plainTxt = uv.getValue();
			}
		}
		
		if(getType() == SearchType.PLAIN) {
			retVal = findNextPlain(plainTxt, charIdx);
		} else if(getType() == SearchType.REGEX) {
			retVal = findNextRegex(plainTxt, charIdx);
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
		
		String plainTxt = FormatterUtil.format(obj);
		if(plainTxt.length() == 0 && obj instanceof IExtendable) {
			final UnvalidatedValue uv = ((IExtendable)obj).getExtension(UnvalidatedValue.class);
			if(uv != null) {
				plainTxt = uv.getValue();
			}
		}
		
		if(getType() == SearchType.PLAIN) {
			retVal = findPrevPlain(plainTxt, charIdx);
		} else if(getType() == SearchType.REGEX) {
			retVal = findPrevRegex(plainTxt, charIdx);
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
		
		if(charIdx < txt.length() && lastMatcher.find(charIdx)) {
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
						LOGGER.error(
								e.getLocalizedMessage(), e);
					}
				}
			}
		}
		
		return retVal;
	}
	
	public Object replacePlain(Object obj, String expr) {
		String plainTxt = FormatterUtil.format(obj);
		
		if(plainTxt.length() == 0 && obj instanceof IExtendable) {
			final UnvalidatedValue uv = ((IExtendable)obj).getExtension(UnvalidatedValue.class);
			if(uv != null) {
				plainTxt = uv.getValue();
			}
		}
		
		final StringBuffer buffer = new StringBuffer();
		if(lastRange != null) {
			buffer.append(plainTxt.substring(0, lastRange.getStart()));
			buffer.append(expr);
			buffer.append(plainTxt.substring(lastRange.getEnd()));
		}
		
		final String newTxt = buffer.toString();
		Object retVal = FormatterUtil.parse(obj.getClass(), newTxt);
		
		if(retVal == null && IExtendable.class.isAssignableFrom(obj.getClass())) {
			try {
				retVal = obj.getClass().newInstance();
				((IExtendable)retVal).putExtension(UnvalidatedValue.class, new UnvalidatedValue(newTxt));
			} catch (InstantiationException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			} catch (IllegalAccessException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		
		if(obj instanceof IPATranscript && ((IExtendable)obj).getExtension(AlternativeTranscript.class) != null) {
			((IExtendable)retVal).putExtension(AlternativeTranscript.class, 
					((IExtendable)obj).getExtension(AlternativeTranscript.class));
		}
		
		return retVal;
	}
	
	public Object replaceRegex(Object obj, String expr) {
		final StringBuffer buffer = new StringBuffer();
		if(lastMatcher != null) {
			lastMatcher.appendReplacement(buffer, expr);
			lastMatcher.appendTail(buffer);
		}
		
		final String newTxt = buffer.toString();
		Object retVal = FormatterUtil.parse(obj.getClass(), newTxt);
		if(retVal == null && IExtendable.class.isAssignableFrom(obj.getClass())) {
			try {
				retVal = obj.getClass().newInstance();
				((IExtendable)retVal).putExtension(UnvalidatedValue.class, new UnvalidatedValue(newTxt));
			} catch (InstantiationException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			} catch (IllegalAccessException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		
		if(obj instanceof IPATranscript && ((IExtendable)obj).getExtension(AlternativeTranscript.class) != null) {
			((IExtendable)retVal).putExtension(AlternativeTranscript.class, 
					((IExtendable)obj).getExtension(AlternativeTranscript.class));
		}
		
		return retVal;
	}
	
	public IPATranscript replacePhonex(IPATranscript ipa, IPATranscript expr) {
		final IPATranscriptBuilder builder = new IPATranscriptBuilder();
		if(lastPhonexMatcher != null) {
			lastPhonexMatcher.appendReplacement(builder, expr);
			lastPhonexMatcher.appendTail(builder);
		}
		final IPATranscript retVal = builder.toIPATranscript();
		if(ipa.getExtension(AlternativeTranscript.class) != null) {
			retVal.putExtension(AlternativeTranscript.class, ipa.getExtension(AlternativeTranscript.class));
		}
		return retVal;
	}
	
}
