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
package ca.phon.session;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;

/**
 * Custom String implementation for tiers with extension support.
 * 
 */
public class TierString
	implements java.io.Serializable, Comparable<String>, CharSequence, IExtendable {
	
	private static final long serialVersionUID = 7079791690885598508L;

	private final String delegate;
	
	private final ExtensionSupport extSupport = new ExtensionSupport(TierString.class, this);
	
	private List<TierString> words = null;
	
	private List<Integer> wordOffsets = null;
	
	public TierString() {
		this(new String());
	}
	
	public TierString(String value) {
		super();
		this.delegate = value;
		
		extSupport.initExtensions();
	}
	
	private void tokenize() {
		words = new ArrayList<>();
		wordOffsets = new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < length(); i++) {
			char c = charAt(i);
			if(Character.isWhitespace(c)) {
				if(sb.length() > 0) {
					final TierString ts = new TierString(sb.toString());
					words.add(ts);
					sb.setLength(0);
					wordOffsets.add(i - ts.length());
				}
			} else {
				sb.append(c);
			}
		}
		if(sb.length() > 0) {
			final TierString ts = new TierString(sb.toString());
			words.add(ts);
			wordOffsets.add(length() - ts.length());
		}
	}
	
	public List<TierString> getWords() {
		if(words == null) {
			tokenize();
		}
		return Collections.unmodifiableList(words);
	}
	
	public int numberOfWords() {
		return getWords().size();
	}
	
	/**
	 * Return the character offset of the start of the given word.
	 * 
	 * @param wordIndex
	 * @return
	 */
	public int getWordOffset(int wordIndex) {
		if(wordOffsets == null)
			tokenize();
		return wordOffsets.get(wordIndex);
	}
	
	public TierString getWord(int wordIndex) {
		return getWords().get(wordIndex);
	}

	public int length() {
		return delegate.length();
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public char charAt(int index) {
		return delegate.charAt(index);
	}

	public int codePointAt(int index) {
		return delegate.codePointAt(index);
	}

	public int codePointBefore(int index) {
		return delegate.codePointBefore(index);
	}

	public int codePointCount(int beginIndex, int endIndex) {
		return delegate.codePointCount(beginIndex, endIndex);
	}

	public int offsetByCodePoints(int index, int codePointOffset) {
		return delegate.offsetByCodePoints(index, codePointOffset);
	}

	public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
		delegate.getChars(srcBegin, srcEnd, dst, dstBegin);
	}

	@Deprecated
	public void getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin) {
		delegate.getBytes(srcBegin, srcEnd, dst, dstBegin);
	}

	public byte[] getBytes(String charsetName) throws UnsupportedEncodingException {
		return delegate.getBytes(charsetName);
	}

	public byte[] getBytes(Charset charset) {
		return delegate.getBytes(charset);
	}

	public byte[] getBytes() {
		return delegate.getBytes();
	}

	public boolean equals(Object anObject) {
		return delegate.equals(anObject);
	}

	public boolean contentEquals(StringBuffer sb) {
		return delegate.contentEquals(sb);
	}

	public boolean contentEquals(CharSequence cs) {
		return delegate.contentEquals(cs);
	}

	public boolean equalsIgnoreCase(String anotherString) {
		return delegate.equalsIgnoreCase(anotherString);
	}

	public int compareTo(String anotherString) {
		return delegate.compareTo(anotherString);
	}

	public int compareToIgnoreCase(String str) {
		return delegate.compareToIgnoreCase(str);
	}

	public boolean regionMatches(int toffset, String other, int ooffset, int len) {
		return delegate.regionMatches(toffset, other, ooffset, len);
	}

	public boolean regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len) {
		return delegate.regionMatches(ignoreCase, toffset, other, ooffset, len);
	}

	public boolean startsWith(String prefix, int toffset) {
		return delegate.startsWith(prefix, toffset);
	}

	public boolean startsWith(String prefix) {
		return delegate.startsWith(prefix);
	}

	public boolean endsWith(String suffix) {
		return delegate.endsWith(suffix);
	}

	public int hashCode() {
		return delegate.hashCode();
	}

	public int indexOf(int ch) {
		return delegate.indexOf(ch);
	}

	public int indexOf(int ch, int fromIndex) {
		return delegate.indexOf(ch, fromIndex);
	}

	public int lastIndexOf(int ch) {
		return delegate.lastIndexOf(ch);
	}

	public int lastIndexOf(int ch, int fromIndex) {
		return delegate.lastIndexOf(ch, fromIndex);
	}

	public int indexOf(String str) {
		return delegate.indexOf(str);
	}

	public int indexOf(String str, int fromIndex) {
		return delegate.indexOf(str, fromIndex);
	}

	public int lastIndexOf(String str) {
		return delegate.lastIndexOf(str);
	}

	public int lastIndexOf(String str, int fromIndex) {
		return delegate.lastIndexOf(str, fromIndex);
	}

	public String substring(int beginIndex) {
		return delegate.substring(beginIndex);
	}

	public String substring(int beginIndex, int endIndex) {
		return delegate.substring(beginIndex, endIndex);
	}

	public CharSequence subSequence(int beginIndex, int endIndex) {
		return delegate.subSequence(beginIndex, endIndex);
	}

	public String concat(String str) {
		return delegate.concat(str);
	}

	public String replace(char oldChar, char newChar) {
		return delegate.replace(oldChar, newChar);
	}

	public boolean matches(String regex) {
		return delegate.matches(regex);
	}

	public boolean contains(CharSequence s) {
		return delegate.contains(s);
	}

	public String replaceFirst(String regex, String replacement) {
		return delegate.replaceFirst(regex, replacement);
	}

	public String replaceAll(String regex, String replacement) {
		return delegate.replaceAll(regex, replacement);
	}

	public String replace(CharSequence target, CharSequence replacement) {
		return delegate.replace(target, replacement);
	}

	public String[] split(String regex, int limit) {
		return delegate.split(regex, limit);
	}

	public String[] split(String regex) {
		return delegate.split(regex);
	}

	public String toLowerCase(Locale locale) {
		return delegate.toLowerCase(locale);
	}

	public String toLowerCase() {
		return delegate.toLowerCase();
	}

	public String toUpperCase(Locale locale) {
		return delegate.toUpperCase(locale);
	}

	public String toUpperCase() {
		return delegate.toUpperCase();
	}

	public String trim() {
		return delegate.trim();
	}

	public String toString() {
		return delegate.toString();
	}

	public char[] toCharArray() {
		return delegate.toCharArray();
	}

	public String intern() {
		return delegate.intern();
	}

	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}
	
	

}
