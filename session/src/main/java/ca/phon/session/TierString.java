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
package ca.phon.session;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

import ca.phon.extensions.*;

/**
 * Custom String implementation for tiers with extension support.
 * 
 */
public class TierString extends ExtendableObject
	implements java.io.Serializable, Comparable<String>, CharSequence {
	
	private static final long serialVersionUID = 7079791690885598508L;

	private final String delegate;
	
	private List<TierString> words = null;
	
	private List<Integer> wordOffsets = null;
	
	public TierString() {
		this(new String());
	}
	
	public TierString(String value) {
		super();
		this.delegate = value;
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

}
