/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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

import ca.phon.app.log.LogUtil;
import ca.phon.extensions.*;
import ca.phon.formatter.FormatterUtil;
import ca.phon.ipa.*;
import ca.phon.orthography.InternalMedia;
import ca.phon.phonex.*;
import ca.phon.session.MediaSegment;
import ca.phon.util.Range;

import java.text.ParseException;
import java.util.regex.*;

/**
 * Find expression used for searching within a transcript.
 *
 * @param type the type of search to perform
 * @param expr the expression to search for, must be valid for the given type
 * @param caseSensitive whether or not the search should be case sensitive
 */
public record FindExpr(SearchType type, String expr, boolean caseSensitive) {

	/**
	 * Find previous occurrence of the expression in the object.
	 *
	 * @param obj the object to search
	 * @param charIdx the character index to start searching from
	 * @return the range for the next occurrence of the expression in the object, or an empty match
	 *  if not found
	 */
	public FindExprMatch findNext(Object obj, int charIdx) {
		FindExprMatch retVal = FindExprMatch.empty();
		
		String plainTxt = FormatterUtil.format(obj);

		// HACK: if the object is a MediaSegment, we need to add the prefix/suffix dots
		if(obj instanceof MediaSegment) {
			plainTxt = InternalMedia.MEDIA_BULLET + plainTxt + InternalMedia.MEDIA_BULLET;
		}
		
		if(type() == SearchType.PLAIN) {
			retVal = findNextPlain(plainTxt, charIdx);
		} else if(type() == SearchType.REGEX) {
			retVal = findNextRegex(plainTxt, charIdx);
		} else if(type() == SearchType.PHONEX) {
			if(obj instanceof IPATranscript) {
				retVal = findNextPhonex((IPATranscript)obj, charIdx);
			}
		}
		
		return retVal;
	}

	/**
	 * Find previous occurrence of the expression in the object.
	 *
	 * @param obj the object to search
	 * @param charIdx the character index to start searching from
	 * @return the range for the previous occurrence of the expression in the object, or null if not found
	 */
	public FindExprMatch findPrev(Object obj, int charIdx) {
		FindExprMatch retVal = FindExprMatch.empty();
		
		String plainTxt = FormatterUtil.format(obj);

		// HACK: if the object is a MediaSegment, we need to add the prefix/suffix dots
		if(obj instanceof MediaSegment) {
			plainTxt = InternalMedia.MEDIA_BULLET + plainTxt + InternalMedia.MEDIA_BULLET;
		}
		
		if(type() == SearchType.PLAIN) {
			retVal = findPrevPlain(plainTxt, charIdx);
		} else if(type() == SearchType.REGEX) {
			retVal = findPrevRegex(plainTxt, charIdx);
		} else if(type() == SearchType.PHONEX) {
			if(obj instanceof IPATranscript) {
				retVal = findPrevPhonex((IPATranscript)obj, charIdx);
			}
		}
		
		return retVal;
	}
	
	public FindExprMatch findNextPlain(String txt, int charIdx) {
		final String expr = (caseSensitive() ? expr() : expr().toLowerCase());
		final String val = (caseSensitive() ? txt : txt.toLowerCase());
		int nextIdx = val.indexOf(expr, charIdx);
		if(nextIdx >= 0) {
			final Range range = new Range(nextIdx, nextIdx + expr.length(), false);
			return new FindExprMatch(range, null, null);
		}
		return FindExprMatch.empty();
	}
	
	public FindExprMatch findPrevPlain(String txt, int charIdx) {
		final String expr = (caseSensitive() ? expr() : expr().toLowerCase());
		final String val = (caseSensitive() ? txt.substring(0, charIdx) : txt.substring(0, charIdx).toLowerCase());
		
		int prevIdx = val.lastIndexOf(expr);
		if(prevIdx >= 0) {
			final Range range = new Range(prevIdx, prevIdx + expr.length(), false);
			return new FindExprMatch(range, null, null);
		}
		return FindExprMatch.empty();
	}
	
	public FindExprMatch findNextRegex(String txt, int charIdx) {
		try {
			final Pattern regexPattern = Pattern.compile(expr(), (caseSensitive() ? 0 : Pattern.CASE_INSENSITIVE));
			final Matcher matcher = regexPattern.matcher(txt);

			if (charIdx < txt.length() && matcher.find(charIdx)) {
				final Range range = new Range(matcher.start(), matcher.end(), false);
				return new FindExprMatch(range, matcher, null);
			}
		} catch (PatternSyntaxException e) {
			// invalid regex, return empty match
		}
		return FindExprMatch.empty();
	}
	
	public FindExprMatch findPrevRegex(String txt, int charIdx) {
		try {
			final Pattern regexPattern = Pattern.compile(expr(), (caseSensitive() ? 0 : Pattern.CASE_INSENSITIVE));
			final Matcher matcher = regexPattern.matcher(txt);
			matcher.region(0, charIdx);

			int start = -1;
			int end = -1;
			while (matcher.find()) {
				start = matcher.start();
				end = matcher.end();
			}
			if (start >= 0 && end >= start) {
				// reset matcher to position
				matcher.find(start);
				final Range range = new Range(start, end, false);
				return new FindExprMatch(range, matcher, null);
			}
		} catch (PatternSyntaxException e) {
			// invalid regex, return empty match
		}
		return null;
	}
	
	public FindExprMatch findNextPhonex(IPATranscript ipa, int charIdx) {
		try {
			final PhonexPattern phonexPattern = PhonexPattern.compile(expr());
			final PhonexMatcher phonexMatcher = phonexPattern.matcher(ipa);

			// convert charIdx to ipa idx
			final int idx = ipa.ipaIndexOf(charIdx);
			if (idx >= 0) {
				if (phonexMatcher.find(idx)) {
					final int ipaStart = phonexMatcher.start();
					final int ipaEnd = phonexMatcher.end();

					final int start = ipa.stringIndexOfElement(ipaStart);
					final int end = ipa.stringIndexOfElement(ipaEnd);

					final Range range = new Range(start, end, false);
					return new FindExprMatch(range, null, phonexMatcher);
				}
			}
		} catch (PhonexPatternException e) {
			// invalid phonex, return empty match
		}
		return FindExprMatch.empty();
	}
	
	public FindExprMatch findPrevPhonex(IPATranscript ipa, int charIdx) {
		try {
			final PhonexPattern phonexPattern = PhonexPattern.compile(expr());
			int lastPhonexIdx = ipa.ipaIndexOf(charIdx);
			if (lastPhonexIdx < 0 && charIdx == ipa.toString().length()) lastPhonexIdx = ipa.length();

			final PhonexMatcher phonexMatcher = phonexPattern.matcher(ipa);
			phonexMatcher.region(0, lastPhonexIdx);

			int ipaStart = -1;
			int ipaEnd = -1;
			while (phonexMatcher.find()) {
				ipaStart = phonexMatcher.start();
				ipaEnd = phonexMatcher.end();
			}

			if (ipaStart >= 0 && ipaEnd >= ipaStart) {
				final int start = ipa.stringIndexOfElement(ipaStart);
				final int end = ipa.stringIndexOfElement(ipaEnd);

				if (start >= 0 && end >= start) {
					phonexMatcher.find(start);
					final Range range = new Range(start, end, false);
					return new FindExprMatch(range, null, phonexMatcher);
				}
			}
		} catch (PhonexPatternException e) {
			// invalid phonex, return empty match
		}
		return FindExprMatch.empty();
	}

//	public Object replace(String expr) {
//		Object retVal = lastObj;
//
//		if(lastObj != null) {
//			if(type == SearchType.PLAIN) {
//				return replacePlain(lastObj, expr);
//			} else if(type == SearchType.REGEX) {
//				return replaceRegex(lastObj, expr);
//			} else if(type == SearchType.PHONEX) {
//				if(retVal instanceof IPATranscript) {
//					try {
//						return replacePhonex((IPATranscript)retVal, IPATranscript.parseIPATranscript(expr));
//					} catch (ParseException e) {
//						LogUtil.warning(e);
//					}
//				}
//			}
//		}
//
//		return retVal;
//	}
//
//	public Object replacePlain(Object obj, String expr) {
//		String plainTxt = FormatterUtil.format(obj);
//
//		if(plainTxt.length() == 0 && obj instanceof IExtendable) {
//			final UnvalidatedValue uv = ((IExtendable)obj).getExtension(UnvalidatedValue.class);
//			if(uv != null) {
//				plainTxt = uv.getValue();
//			}
//		}
//
//		final StringBuffer buffer = new StringBuffer();
//		if(lastRange != null) {
//			buffer.append(plainTxt.substring(0, lastRange.getStart()));
//			buffer.append(expr);
//			buffer.append(plainTxt.substring(lastRange.getEnd()));
//		}
//
//		final String newTxt = buffer.toString();
//		Object retVal = FormatterUtil.parse(obj.getClass(), newTxt);
//
//		if(retVal == null && IExtendable.class.isAssignableFrom(obj.getClass())) {
//			try {
//				retVal = obj.getClass().newInstance();
//				((IExtendable)retVal).putExtension(UnvalidatedValue.class, new UnvalidatedValue(newTxt));
//			} catch (InstantiationException | IllegalAccessException e) {
//				LogUtil.warning(e);
//			}
//		}
//
//		return retVal;
//	}
//
//	public Object replaceRegex(Object obj, String expr) {
//		final StringBuffer buffer = new StringBuffer();
//		if(lastMatcher != null) {
//			lastMatcher.appendReplacement(buffer, expr);
//			lastMatcher.appendTail(buffer);
//		}
//
//		final String newTxt = buffer.toString();
//		Object retVal = FormatterUtil.parse(obj.getClass(), newTxt);
//		if(retVal == null && IExtendable.class.isAssignableFrom(obj.getClass())) {
//			try {
//				retVal = obj.getClass().newInstance();
//				((IExtendable)retVal).putExtension(UnvalidatedValue.class, new UnvalidatedValue(newTxt));
//			} catch (InstantiationException | IllegalAccessException e) {
//				LogUtil.warning(e);
//			}
//		}
//
//		return retVal;
//	}
//
//	public IPATranscript replacePhonex(IPATranscript ipa, IPATranscript expr) {
//		final IPATranscriptBuilder builder = new IPATranscriptBuilder();
//		if(lastPhonexMatcher != null) {
//			lastPhonexMatcher.appendReplacement(builder, expr);
//			lastPhonexMatcher.appendTail(builder);
//		}
//		final IPATranscript retVal = builder.toIPATranscript();
//		return retVal;
//	}
	
}
