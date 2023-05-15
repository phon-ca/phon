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
import java.util.Collections;
import java.util.List;

/**
 * Two words joined by a marker.
 */
public final class CompoundWord extends Word {
	
	private final Word word1;
	
	private final CompoundWordMarker marker;
	
	private final Word word2;
	
	public CompoundWord(Word word1, Word word2) {
		this(word1, word2, CompoundWordMarkerType.COMPOUND);
	}
	
	public CompoundWord(Word word1, Word word2, CompoundWordMarkerType marker) {
		this(word1, word2, new CompoundWordMarker(marker));
	}

	public CompoundWord(Word word1, Word word2, CompoundWordMarker marker) {
		this(null, null, word1, word2, marker);
	}

	public CompoundWord(WordPrefix prefix, WordSuffix suffix, Word word1, Word word2, CompoundWordMarker marker) {
		this(new Langs(), prefix, suffix, word1, word2, marker);
	}

	public CompoundWord(Langs langs, WordPrefix prefix, WordSuffix suffix, Word word1, Word word2, CompoundWordMarker marker) {
		super(langs, prefix, suffix, null, new WordElement[0]);
		this.word1 = word1;
		this.word2 = word2;
		this.marker = marker;
	}
	
	public Word getWord1() {
		return word1;
	}

	public CompoundWordMarker getMarker() {
		return marker;
	}

	public Word getWord2() {
		return word2;
	}

	@Override
	public List<WordElement> getWordElements() {
		final List<WordElement> elements = new ArrayList<>();
		elements.addAll(getWord1().getWordElements());
		elements.add(getMarker());
		elements.addAll(getWord2().getWordElements());
		return Collections.unmodifiableList(elements);
	}

}
