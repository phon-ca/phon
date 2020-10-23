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
package ca.phon.ipa;

import java.util.*;

import ca.phon.visitor.*;
import ca.phon.visitor.annotation.*;

/**
 * Breaks a transcript into individual words.
 */
public class WordVisitor extends VisitorAdapter<IPAElement> {

	/**
	 * list of detected syllables
	 */
	private final List<IPATranscript> words = new ArrayList<IPATranscript>();
	
	/**
	 * current syllable
	 * 
	 */
	private IPATranscriptBuilder currentWordBuilder = new IPATranscriptBuilder();
	
	@Override
	public void fallbackVisit(IPAElement obj) {
		appendWord(obj);
	}
	
	@Visits
	public void visitWordBoundary(WordBoundary wb) {
		breakWord();
	}
	
	private void appendWord(IPAElement e) {
		currentWordBuilder.append(e);
	}
	
	private void breakWord() {
		if(currentWordBuilder.toIPATranscript().length() > 0) {
			words.add(currentWordBuilder.toIPATranscript());
			currentWordBuilder = new IPATranscriptBuilder();
		}
	}
	
	public List<IPATranscript> getWords() {
		breakWord();
		return this.words;
	}
	
}
