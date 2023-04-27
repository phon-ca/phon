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
public class OrthoWord extends AbstractOrthoElement {

	private final WordPrefix prefix;

	private final WordSuffix suffix;

	private final UntranscribedType untranscribed;

	private final List<OrthoWordElement> wordElements;

	public OrthoWord(String text) {
		this(new OrthoWordText(text));
	}

	public OrthoWord(OrthoWordElement ... wordElements) {
		this(null, null, wordElements);
	}

	public OrthoWord(WordPrefix prefix, WordSuffix suffix, OrthoWordElement ... wordElements) {
		this(prefix, suffix, null, wordElements);
	}

	public OrthoWord(WordPrefix prefix, WordSuffix suffix, UntranscribedType untranscribedType, OrthoWordElement ... wordElements) {
		super();
		this.prefix = prefix;
		this.suffix = suffix;
		this.untranscribed = untranscribedType;
		this.wordElements = new ArrayList<>();
		this.wordElements.addAll(Arrays.asList(wordElements));
	}

	public OrthoWord(String text, UntranscribedType untranscribed) {
		this(null, null, untranscribed, new OrthoWordText(text));
	}

	public OrthoWord(String text, WordType prefix) {
		this(text, prefix, null);
	}

	public OrthoWord(String text, WordFormType suffix) {
		this(text, null, suffix);
	}

	public OrthoWord(String text, WordType prefix, WordFormType suffix) {
		this(text, new WordPrefix(prefix), new WordSuffix(suffix));
	}

	public OrthoWord(String text, WordPrefix prefix, WordSuffix suffix) {
		this(text, prefix, suffix, null);
	}

	public OrthoWord(String text, WordPrefix prefix, WordSuffix suffix, UntranscribedType untranscribed) {
		this(prefix, suffix, untranscribed, new OrthoWordText(text));
	}

	public OrthoWord(String text, WordType prefix, WordFormType suffix, UntranscribedType untranscribed) {
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

	/**
	 * Get the root word data without prefix/suffix,
	 * ca-elements, ca-delimiters, etc.
	 *
	 * @return the root word
	 */
	public String getWord() {
		final List<OrthoWordElement> textElements =
				this.wordElements.stream().filter((ele) -> ele instanceof OrthoWordText).toList();
		return textElements.stream().map((ele) -> ele.text()).collect(Collectors.joining());
	}

	/**
	 * Return an unmodifiable list of word elements
	 *
	 * @return wordElements
	 */
	public List<OrthoWordElement> getWordElements() {
		return Collections.unmodifiableList(this.wordElements);
	}

	@Override
	public String text() {
		return (
				(this.prefix == null ? "" : this.prefix) +
						this.wordElements.stream().map((ele) -> ele.text()).collect(Collectors.joining()) +
						(this.suffix == null ? "" : this.suffix)
		);
	}

	@Override
	public String toString() {
		return text();
	}

}
