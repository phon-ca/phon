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

/**
 * Phone visitor for filtering punctuation in transcriptions.
 * Flag to ignore or include word boundaries can be set during
 * construction.
 */
public class PunctuationFilter extends VisitorAdapter<IPAElement> {
	
	/**
	 * filtered transcript
	 */
	private final IPATranscriptBuilder builder;
	
	private final boolean ignoreWordBoundaries;
	
	public PunctuationFilter() {
		this(false);
	}
	
	public PunctuationFilter(boolean ignoreWordBoundaries) {
		builder = new IPATranscriptBuilder();
		this.ignoreWordBoundaries = ignoreWordBoundaries;
	}

	@Override
	public void fallbackVisit(IPAElement obj) {
		builder.append(obj);
	}
	
	@Visits
	public void visitStressMarker(StressMarker stressMarker) {
		// don't add
	}
	
	@Visits
	public void visitSyllableBoundaryMarker(SyllableBoundary boundary) {
		// don't add
	}
	
	@Visits
	public void visitSandhi(Sandhi sandhi) {
		// don't add
	}
	
	@Visits
	public void visitCompoundWordMarker(CompoundWordMarker wordMarker) {
		// don't add
	}
	
	@Visits
	public void visitWordBoundary(WordBoundary wb) {
		if(!ignoreWordBoundaries)
			builder.appendWordBoundary();
	}
	
	public IPATranscript getIPATranscript() {
		return builder.toIPATranscript();
	}
	
}
