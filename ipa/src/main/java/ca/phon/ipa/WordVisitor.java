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

import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.util.*;

/**
 * Breaks a transcript into individual words or phonetic groups.
 */
public class WordVisitor extends VisitorAdapter<IPAElement> {

	/**
	 * list of words or phonetic groups
	 */
	private final Stack<List<IPATranscript>> wordListStack;

	public WordVisitor() {
		super();
		this.wordListStack = new Stack<>();
		this.wordListStack.push(new ArrayList<>());
	}
	
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

	@Visits
	public void visitPgMarker(PhoneticGroupMarker pgm) {
		if(pgm.getType() == PhoneticGroupMarkerType.BEGIN) {
			wordListStack.push(new ArrayList<>());
			this.currentWordBuilder.appendPgStart();
		} else {
			this.currentWordBuilder.appendPgEnd();
			breakWord();
			popPhoneticGroup();
		}
	}

	private void popPhoneticGroup() {
		final IPATranscriptBuilder builder = new IPATranscriptBuilder();
		final List<IPATranscript> pgStack = wordListStack.pop();
		for(IPATranscript pgWord:pgStack) {
			if(builder.size() > 0) builder.appendWordBoundary();
			builder.append(pgWord);
		}
		wordListStack.peek().add(builder.toIPATranscript());
	}

	private void appendWord(IPAElement e) {
		currentWordBuilder.append(e);
	}
	
	private void breakWord() {
		if(currentWordBuilder.toIPATranscript().length() > 0) {
			wordListStack.peek().add(currentWordBuilder.toIPATranscript());
			currentWordBuilder = new IPATranscriptBuilder();
		}
	}
	
	public List<IPATranscript> getWords() {
		breakWord();
		while(wordListStack.size() > 1) {
			popPhoneticGroup();
		}
		return this.wordListStack.peek();
	}
	
}
