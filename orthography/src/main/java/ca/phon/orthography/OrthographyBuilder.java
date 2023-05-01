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
	 * Internal list of {@link OrthographyElement}s
	 */
	private final List<OrthographyElement> eleList = new ArrayList<OrthographyElement>();

	private final List<WordElement> wordElements = new ArrayList<>();

	public OrthographyBuilder() {
	}
	
	public OrthographyBuilder clear() {
		eleList.clear();
		return this;
	}
	
	public OrthographyBuilder append(Orthography ortho) {
		for(OrthographyElement ele:ortho) eleList.add(ele);
		return this;
	}
	
	public OrthographyBuilder append(OrthographyElement ele) {
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
		final Word word = new Word(data, prefix, suffix, untranscribed);
		eleList.add(word);
		return this;
	}
	
	public OrthographyBuilder appendWord(String data, WordType prefix, WordFormType suffix) {
		final Word word = new Word(data, prefix, suffix);
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

	public OrthographyBuilder appendWord(WordElement... wordElements) {
		return appendWord(null, null, null, wordElements);
	}

	public OrthographyBuilder appendWord(WordSuffix suffix, WordElement... wordElements) {
		return appendWord(null, suffix, null, wordElements);
	}

	public OrthographyBuilder appendWord(WordPrefix prefix, WordElement... wordElements) {
		return appendWord(prefix, null, null, wordElements);
	}

	public OrthographyBuilder appendWord(WordPrefix prefix, WordSuffix suffix, WordElement... wordElements) {
		return appendWord(prefix, suffix, null, wordElements);
	}

	public OrthographyBuilder appendWord(WordPrefix prefix, WordSuffix suffix, UntranscribedType untranscribedType, WordElement... wordElements) {
		return append(new Word(prefix, suffix, untranscribedType, wordElements));
	}

	public OrthographyBuilder appendWord(List<WordElement> wordElements) {
		return appendWord(null, null, null, wordElements);
	}

	public OrthographyBuilder appendWord(WordSuffix suffix, List<WordElement> wordElements) {
		return appendWord(null, suffix, null, wordElements);
	}

	public OrthographyBuilder appendWord(WordPrefix prefix, List<WordElement> wordElements) {
		return appendWord(prefix, null, null, wordElements);
	}

	public OrthographyBuilder appendWord(WordPrefix prefix, WordSuffix suffix, List<WordElement> wordElements) {
		return appendWord(prefix, suffix, null, wordElements);
	}

	public OrthographyBuilder appendWord(WordPrefix prefix, WordSuffix suffix, UntranscribedType untranscribedType, List<WordElement> wordElements) {
		return appendWord(prefix, suffix, untranscribedType, wordElements.toArray(new WordElement[0]));
	}

	public OrthographyBuilder appendTagMarker(TagMarkerType tmType) {
		append(new TagMarker(tmType));
		return this;
	}
	
	public OrthographyBuilder appendComment(String type, String data) {
		final OrthographyComment comment = new OrthographyComment(type, data);
		eleList.add(comment);
		return this;
	}
	
	public OrthographyBuilder appendComment(String data) {
		appendComment(null, data);
		return this;
	}
	
	public OrthographyBuilder appendCompoundWord(Word word1, Word word2, CompoundWordMarkerType marker) {
		final CompoundWord wordnet = new CompoundWord(word1, word2, marker);
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
	 * @throws IllegalStateException if the previous element is not an {@link Word} or
	 *  the element list is empty
	 */
	public OrthographyBuilder createCompoundWord(Word word2, CompoundWordMarkerType marker) {
		if(eleList.size() == 0)
			throw new IllegalStateException("Unable to create wordnet from empty list");
		final OrthographyElement prevEle = eleList.get(eleList.size()-1);
		if(!(prevEle instanceof Word))
			throw new IllegalStateException("Unable to create wordnet, previous element not a word.");
		return appendCompoundWord((Word)prevEle, word2, marker);
	}
	
	/**
	 * Creates a wordnet using the last two elements.
	 * 
	 * @param marker
	 * @return
	 * 
	 * @throws IllegalStateException if the previous two elements are not {@link Word}s or
	 *  the element list is empty 
	 */
	public OrthographyBuilder createCompoundWord(CompoundWordMarkerType marker) {
		if(eleList.size() < 2)
			throw new IllegalStateException("Unable to create wordnet, not enough elements.");
		final OrthographyElement ele1 = eleList.get(eleList.size()-2);
		if(!(ele1 instanceof Word))
			throw new IllegalStateException("Unable to create wordnet, both elements must be words");
		final OrthographyElement ele2 = eleList.get(eleList.size()-1);
		if(!(ele2 instanceof Word))
			throw new IllegalStateException("Unable to create wordnet, both elements must be words");
		eleList.remove(eleList.size()-1);
		eleList.remove(eleList.size()-1);
		return appendCompoundWord((Word)ele1, (Word)ele2, marker);
	}
	
	/**
	 * Creates a wordnet using the last two elements.
	 * 
	 * @return
	 * 
	 * @throws IllegalStateException if the previous two elements are not {@link Word}s or
	 *  the element list is empty 
	 */
	public OrthographyBuilder createCompoundWord() {
		return createCompoundWord(CompoundWordMarkerType.COMPOUND);
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
		final OrthographyElement ele = eleList.get(eleList.size()-1);
		if(ele instanceof CompoundWord) {
			final CompoundWord compoundWord = (CompoundWord) ele;
			final CompoundWord annotatedWord = new CompoundWord(wordPrefix, wordSuffix, compoundWord.getWord1(), compoundWord.getWord2(), compoundWord.getMarker());
			eleList.remove(eleList.size()-1);
			eleList.add(annotatedWord);
		} else if(ele instanceof Word) {
			final Word word = (Word) ele;
			final Word annotatedWord = new Word(wordPrefix, wordSuffix, untranscribedType, word.getWordElements().toArray(new WordElement[0]));
			eleList.remove(eleList.size()-1);
			eleList.add(annotatedWord);
		} else {
			throw new IllegalStateException("Unable to annotate word, last element is not a word");
		}
		return this;
	}
	
	public OrthographyBuilder appendPunct(OrthoPunctType type) {
		final OrthographyPunct punct = new OrthographyPunct(type);
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
	
	public OrthographyElement elementAt(int idx) {
		return eleList.get(idx);
	}

	public OrthographyElement lastElement() {
		if(size() > 0)
			return elementAt(size()-1);
		else
			return null;
	}

	public OrthographyElement replaceLastElement(OrthographyElement ele) {
		OrthographyElement retVal = lastElement();
		if(size() > 0)
			eleList.remove(size()-1);
		eleList.add(ele);
		return retVal;
	}
	
	public Orthography toOrthography() {
		return new Orthography(eleList);
	}
	
}
