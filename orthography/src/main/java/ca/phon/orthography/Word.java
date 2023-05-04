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
package ca.phon.orthography;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A word in an orthographic phrase.
 * Words may have prefix and/or suffix
 * codes.
 */
public class Word extends AbstractOrthographyElement {

	private final Langs langs;

	private final WordPrefix prefix;

	private final WordSuffix suffix;

	private final UntranscribedType untranscribed;

	private final List<WordElement> wordElements;

	private final Replacement replacement;

	public Word(String text) {
		this(new WordText(text));
	}

	public Word(WordElement... wordElements) {
		this(null, null, wordElements);
	}

	public Word(WordPrefix prefix, WordSuffix suffix, WordElement... wordElements) {
		this(new Langs(), prefix, suffix, null, wordElements);
	}

	public Word(Langs langs, WordPrefix prefix, WordSuffix suffix, UntranscribedType untranscribedType, WordElement... wordElements) {
		this(langs, null, prefix, suffix, untranscribedType, wordElements);
	}

	public Word(Langs langs, Replacement replacement, WordPrefix prefix, WordSuffix suffix, UntranscribedType untranscribedType, WordElement... wordElements) {
		super();
		this.langs = langs;
		this.replacement = replacement;
		this.prefix = prefix;
		this.suffix = suffix;
		this.untranscribed = untranscribedType;
		this.wordElements = new ArrayList<>();
		this.wordElements.addAll(Arrays.asList(wordElements));
	}

	public Word(String text, UntranscribedType untranscribed) {
		this(new Langs(), null, null, untranscribed, new WordText(text));
	}

	public Word(String text, WordType prefix) {
		this(text, prefix, null);
	}

	public Word(String text, WordFormType suffix) {
		this(text, null, suffix);
	}

	public Word(String text, WordType prefix, WordFormType suffix) {
		this(text, new WordPrefix(prefix), new WordSuffix(suffix));
	}

	public Word(String text, WordPrefix prefix, WordSuffix suffix) {
		this(text, prefix, suffix, null);
	}

	public Word(String text, WordPrefix prefix, WordSuffix suffix, UntranscribedType untranscribed) {
		this(new Langs(), prefix, suffix, untranscribed, new WordText(text));
	}

	public Word(String text, WordType prefix, WordFormType suffix, UntranscribedType untranscribed) {
		this(text, new WordPrefix(prefix), new WordSuffix(suffix), untranscribed);
	}

	public boolean isUntranscribed() {
		return this.untranscribed != null;
	}

	public UntranscribedType getUntranscribedType() {
		return this.untranscribed;
	}

	/**
	 * Get prefix for word.
	 *
	 * @return the word prefix, or <code>null</code> if
	 *  none
	 */
	public WordPrefix getPrefix() {
		return this.prefix;
	}

	/**
	 * Get suffix for word.
	 *
	 * @return the word suffix, or <code>null</code> if
	 *  none
	 */
	public WordSuffix getSuffix() {
		return this.suffix;
	}

	public Langs getLangs() {
		return this.langs;
	}

	public Replacement getReplacement() {
		return this.replacement;
	}

	/**
	 * Get the root word data without prefix/suffix,
	 * ca-elements, ca-delimiters, etc.
	 *
	 * @return the root word
	 */
	public String getWord() {
		final List<WordElement> textElements =
				this.wordElements.stream().filter((ele) -> ele instanceof WordText).toList();
		return textElements.stream().map((ele) -> ele.text()).collect(Collectors.joining());
	}

	/**
	 * Return an unmodifiable list of word elements
	 *
	 * @return wordElements
	 */
	public List<WordElement> getWordElements() {
		return Collections.unmodifiableList(this.wordElements);
	}

	@Override
	public String text() {
		String retVal =
				(this.prefix == null ? "" : this.prefix) +
						this.wordElements.stream().map((ele) -> ele.text()).collect(Collectors.joining()) +
						(this.langs == null ? "" : this.langs) +
						(this.suffix == null ? "" : this.suffix);
		if(getReplacement() != null) {
			retVal += " " + replacement.text();
		}
		return retVal;
	}

	@Override
	public String toString() {
		return text();
	}

}
