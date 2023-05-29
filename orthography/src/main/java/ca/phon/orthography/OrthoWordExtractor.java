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
public class OrthoWordExtractor extends AbstractOrthographyVisitor {

	private final List<Word> wordList = new ArrayList<Word>();
	
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
	@Override
	public void visitWord(Word word) {
		if(word.getPrefix() != null && word.getPrefix().getType() == WordType.OMISSION && !isIncludeOmitted())
			return;
		wordList.add(word);
	}

	@Visits
	@Override
	public void visitCompoundWord(CompoundWord wordnet) {
		wordList.add(wordnet);
	}

	@Visits
	@Override
	public void visitOrthoGroup(OrthoGroup group) {
		group.getElements().forEach(this::visit);
	}

	@Override
	public void visitPhoneticGroup(PhoneticGroup phoneticGroup) {
		phoneticGroup.getElements().forEach(this::visit);
	}

	public List<Word> getWordList() {
		return Collections.unmodifiableList(this.wordList);
	}

	@Override
	public void fallbackVisit(OrthographyElement obj) {
	}
	
}
