/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
	}
	
	@Visits
	public void visitPause(Pause pause) {
		builder.append(pause);
	}
	
	@Visits
	public void visitBasicPhone(Phone phone) {
		builder.append(phone);
	}
	
	@Visits
	public void visitCompoundPhone(CompoundPhone phone) {
		builder.append(phone);
	}
	
	@Visits
	public void visitWordBoundary(WordBoundary wb) {
		if(!ignoreWordBoundaries)
			builder.appendWordBoundary();
	}
	
	@Visits
	public void visitAlignmentMarker(AlignmentMarker marker) {
		builder.append((new IPAElementFactory()).createAlignmentMarker());
	}
	
	public IPATranscript getIPATranscript() {
		return builder.toIPATranscript();
	}
}