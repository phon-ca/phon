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

import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.util.*;

/**
 * Removes any comments, events, or punctuation
 * from Orthography.  This is used for detecting
 * the number of align-able elements in an Orthography instance.
 *
 */
public class OrthoWordExtractor extends VisitorAdapter<OrthoElement> {

	private final List<OrthoElement> wordList = new ArrayList<OrthoElement>();
	
	private boolean includeOmitted = false;
	
	public OrthoWordExtractor() {
		this(false);
	}
	
	public OrthoWordExtractor(boolean includeOmitted) {
		super();
		this.includeOmitted = includeOmitted;
	}
	
	public boolean isIncludeOmitted() {
		return this.includeOmitted;
	}
	
	public void setIncludeOmitted(boolean includeOmitted) {
		this.includeOmitted = includeOmitted;
	}
	
	@Visits
	public void visitWord(OrthoWord word) {
		if(word.getPrefix() != null && word.getPrefix().getType() == WordType.OMISSION)
			return;
		
		wordList.add(word);
	}

	@Visits
	public void visitWordnet(OrthoCompoundWord wordnet) {
		wordList.add(wordnet);
	}
	
	@Visits
	public void visitComment(OrthoComment comment) {
		if(comment.getData().matches("\\.{1,3}")) {
			// add pause as an alignment element
			wordList.add(comment);
		}
	}
	
	public List<OrthoElement> getWordList() {
		return this.wordList;
	}

	@Override
	public void fallbackVisit(OrthoElement obj) {
	}
	
}
