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

/**
 * Two words joined by a marker.
 */
public final class OrthoCompoundWord extends OrthoWord {
	
	private final OrthoWord word1;
	
	private final OrthoCompoundWordMarker marker;
	
	private final OrthoWord word2;
	
	public OrthoCompoundWord(OrthoWord word1, OrthoWord word2) {
		this(word1, word2, OrthoCompoundWordMarkerType.COMPOUND);
	}
	
	public OrthoCompoundWord(OrthoWord word1, OrthoWord word2, OrthoCompoundWordMarkerType marker) {
		this(word1, word2, new OrthoCompoundWordMarker(marker));
	}

	public OrthoCompoundWord(OrthoWord word1, OrthoWord word2, OrthoCompoundWordMarker marker) {
		this(null, null, word1, word2, marker);
	}

	public OrthoCompoundWord(WordPrefix prefix, WordSuffix suffix, OrthoWord word1, OrthoWord word2, OrthoCompoundWordMarker marker) {
		super(prefix, suffix, new OrthoWordElement[0]);
		this.word1 = word1;
		this.word2 = word2;
		this.marker = marker;
	}
	
	public OrthoWord getWord1() {
		return word1;
	}

	public OrthoCompoundWordMarker getMarker() {
		return marker;
	}

	public OrthoWord getWord2() {
		return word2;
	}

	@Override
	public String text() {
		return word1.toString() + marker.getText() + word2.toString();
	}
	
	@Override
	public String toString() {
		return text();
	}

}
