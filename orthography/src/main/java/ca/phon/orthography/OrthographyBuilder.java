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

import java.text.ParseException;
import java.util.*;

/**
 * Class for building {@link Orthography} from various sources.  This class
 * is <i>not</i> thread-safe.
 *
 */
public final class OrthographyBuilder {
	
	/**
	 * Internal list of {@link OrthoElement}s
	 */
	private final List<OrthoElement> eleList = new ArrayList<OrthoElement>();

	private final List<OrthoWordElement> wordElements = new ArrayList<>();

	public OrthographyBuilder() {
	}
	
	public OrthographyBuilder clear() {
		eleList.clear();
		return this;
	}
	
	public OrthographyBuilder append(Orthography ortho) {
		for(OrthoElement ele:ortho) eleList.add(ele);
		return this;
	}
	
	public OrthographyBuilder append(OrthoElement ele) {
		eleList.add(ele);
		return this;
	}
	
	/**
	 * 
	 * @param txt
	 * @return
	 * 
	 * @throws IllegalArgumentException if txt could not be compiled
	 */
	public OrthographyBuilder append(String txt) {
		try {
			final Orthography ortho = Orthography.parseOrthography(txt);
			eleList.addAll(ortho.toList());
		} catch (ParseException pe) {
			throw new IllegalArgumentException(pe);
		}
		return this;
	}
	
	public OrthographyBuilder appendWord(String data, WordType prefix, WordFormType suffix,
										 UntranscribedType untranscribed) {
		final OrthoWord word = new OrthoWord(data, prefix, suffix, untranscribed);
		eleList.add(word);
		return this;
	}
	
	public OrthographyBuilder appendWord(String data, WordType prefix, WordFormType suffix) {
		final OrthoWord word = new OrthoWord(data, prefix, suffix);
		eleList.add(word);
		return this;
	}
	
	public OrthographyBuilder appendWord(String data, WordType prefix) {
		appendWord(data, prefix, null);
		return this;
	}
	
	public OrthographyBuilder appendWord(String data, WordFormType suffix) {
		appendWord(data, null, suffix);
		return this;
	}
	
	public OrthographyBuilder appendWord(String data) {
		appendWord(data, null, null);
		return this;
	}

	public OrthographyBuilder appendWord(OrthoWordElement ... wordElements) {
		return appendWord(null, null, null, wordElements);
	}

	public OrthographyBuilder appendWord(WordSuffix suffix, OrthoWordElement ... wordElements) {
		return appendWord(null, suffix, null, wordElements);
	}

	public OrthographyBuilder appendWord(WordPrefix prefix, OrthoWordElement ... wordElements) {
		return appendWord(prefix, null, null, wordElements);
	}

	public OrthographyBuilder appendWord(WordPrefix prefix, WordSuffix suffix, OrthoWordElement ... wordElements) {
		return appendWord(prefix, suffix, null, wordElements);
	}

	public OrthographyBuilder appendWord(WordPrefix prefix, WordSuffix suffix, UntranscribedType untranscribedType, OrthoWordElement ... wordElements) {
		return append(new OrthoWord(prefix, suffix, untranscribedType, wordElements));
	}

	public OrthographyBuilder appendWord(List<OrthoWordElement> wordElements) {
		return appendWord(null, null, null, wordElements);
	}

	public OrthographyBuilder appendWord(WordSuffix suffix, List<OrthoWordElement> wordElements) {
		return appendWord(null, suffix, null, wordElements);
	}

	public OrthographyBuilder appendWord(WordPrefix prefix, List<OrthoWordElement> wordElements) {
		return appendWord(prefix, null, null, wordElements);
	}

	public OrthographyBuilder appendWord(WordPrefix prefix, WordSuffix suffix, List<OrthoWordElement> wordElements) {
		return appendWord(prefix, suffix, null, wordElements);
	}

	public OrthographyBuilder appendWord(WordPrefix prefix, WordSuffix suffix, UntranscribedType untranscribedType, List<OrthoWordElement> wordElements) {
		return appendWord(prefix, suffix, untranscribedType, wordElements.toArray(new OrthoWordElement[0]));
	}

	public OrthographyBuilder appendTagMarker(TagMarkerType tmType) {
		append(new TagMarker(tmType));
		return this;
	}
	
	public OrthographyBuilder appendComment(String type, String data) {
		final OrthoComment comment = new OrthoComment(type, data);
		eleList.add(comment);
		return this;
	}
	
	public OrthographyBuilder appendComment(String data) {
		appendComment(null, data);
		return this;
	}
	
	public OrthographyBuilder appendCompoundWord(OrthoWord word1, OrthoWord word2, OrthoCompoundWordMarkerType marker) {
		final OrthoCompoundWord wordnet = new OrthoCompoundWord(word1, word2, marker);
		eleList.add(wordnet);
		return this;
	}
	
	/**
	 * Creates a wordnet using the previously added word and the given word.
	 * 
	 * @param word2
	 * @param marker
	 * @return
	 * 
	 * @throws IllegalStateException if the previous element is not an {@link OrthoWord} or
	 *  the element list is empty
	 */
	public OrthographyBuilder createCompoundWord(OrthoWord word2, OrthoCompoundWordMarkerType marker) {
		if(eleList.size() == 0)
			throw new IllegalStateException("Unable to create wordnet from empty list");
		final OrthoElement prevEle = eleList.get(eleList.size()-1);
		if(!(prevEle instanceof OrthoWord))
			throw new IllegalStateException("Unable to create wordnet, previous element not a word.");
		return appendCompoundWord((OrthoWord)prevEle, word2, marker);
	}
	
	/**
	 * Creates a wordnet using the last two elements.
	 * 
	 * @param marker
	 * @return
	 * 
	 * @throws IllegalStateException if the previous two elements are not {@link OrthoWord}s or
	 *  the element list is empty 
	 */
	public OrthographyBuilder createCompoundWord(OrthoCompoundWordMarkerType marker) {
		if(eleList.size() < 2)
			throw new IllegalStateException("Unable to create wordnet, not enough elements.");
		final OrthoElement ele1 = eleList.get(eleList.size()-2);
		if(!(ele1 instanceof OrthoWord))
			throw new IllegalStateException("Unable to create wordnet, both elements must be words");
		final OrthoElement ele2 = eleList.get(eleList.size()-1);
		if(!(ele2 instanceof OrthoWord))
			throw new IllegalStateException("Unable to create wordnet, both elements must be words");
		return appendCompoundWord((OrthoWord)ele1, (OrthoWord)ele2, marker);
	}
	
	/**
	 * Creates a wordnet using the last two elements.
	 * 
	 * @return
	 * 
	 * @throws IllegalStateException if the previous two elements are not {@link OrthoWord}s or
	 *  the element list is empty 
	 */
	public OrthographyBuilder createCompoundWord() {
		return createCompoundWord(OrthoCompoundWordMarkerType.COMPOUND);
	}

	/**
	 * Annotate the previously added word with the given prefix, suffix and untranscribedType
	 *
	 * @param wordPrefix
	 * @param wordSuffix
	 * @param untranscribedType
	 * @return
	 */
	public OrthographyBuilder annnotateWord(WordPrefix wordPrefix, WordSuffix wordSuffix, UntranscribedType untranscribedType) {
		if(eleList.size() < 1)
			throw new IllegalStateException("Unable to annotate word, no data");
		final OrthoElement ele = eleList.get(eleList.size()-1);
		if(ele instanceof OrthoCompoundWord) {
			final OrthoCompoundWord compoundWord = (OrthoCompoundWord) ele;
			final OrthoCompoundWord annotatedWord = new OrthoCompoundWord(wordPrefix, wordSuffix, compoundWord.getWord2(), compoundWord.getWord2(), compoundWord.getMarker());
			eleList.remove(eleList.size()-1);
			eleList.add(annotatedWord);
		} else if(ele instanceof OrthoWord) {
			final OrthoWord word = (OrthoWord) ele;
			final OrthoWord annotatedWord = new OrthoWord(wordPrefix, wordSuffix, untranscribedType, word.getWordElements().toArray(new OrthoWordElement[0]));
			eleList.remove(eleList.size()-1);
			eleList.add(annotatedWord);
		} else {
			throw new IllegalStateException("Unable to annotate word, last element is not a word");
		}
		return this;
	}
	
	public OrthographyBuilder appendPunct(OrthoPunctType type) {
		final OrthoPunct punct = new OrthoPunct(type);
		eleList.add(punct);
		return this;
	}
	
	/**
	 * Append punctuation.
	 * @param punct
	 * @return
	 * 
	 * @throws IllegalArgumentException if the given text is not valid
	 */
	public OrthographyBuilder appendPunct(char punct) {	
		final OrthoPunctType type = OrthoPunctType.fromChar(punct);
		return appendPunct(type);
	}
	
	public int size() {
		return eleList.size();
	}
	
	public OrthoElement elementAt(int idx) {
		return eleList.get(idx);
	}
	
	public OrthographyBuilder appendEvent(String type, String data) {
		final OrthoEvent evt = new OrthoEvent(type, data);
		eleList.add(evt);
		return this;
	}
	
	public Orthography toOrthography() {
		return new Orthography(eleList);
	}
	
}
