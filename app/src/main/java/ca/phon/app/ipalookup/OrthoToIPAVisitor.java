/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.app.ipalookup;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.orthography.OrthoComment;
import ca.phon.orthography.OrthoWord;
import ca.phon.orthography.OrthoWordnet;
import ca.phon.orthography.Orthography;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Create an {@link IPATranscript} from the given {@link Orthography}.  The
 * {@link Orthography} should be visited by the {@link OrthoLookupVisitor} first.
 * 
 */
public class OrthoToIPAVisitor extends VisitorAdapter<Orthography> {
	
	private final IPATranscriptBuilder builder = new IPATranscriptBuilder();

	@Override
	public void fallbackVisit(Orthography obj) {
	}

	@Visits
	public void visitWord(OrthoWord word) {
		final OrthoWordIPAOptions opts = word.getExtension(OrthoWordIPAOptions.class);
		if(opts != null) {
			final String selectedOption = 
					opts.getOptions().get(opts.getSelectedOption());
			if(builder.size() > 0) builder.appendWordBoundary();
			builder.append(selectedOption);
		}
	}
	
	@Visits
	public void visitWordnet(OrthoWordnet wordnet) {
		final OrthoWord word1 = wordnet.getWord1();
		final OrthoWordIPAOptions word1opts = word1.getExtension(OrthoWordIPAOptions.class);
		final OrthoWord word2 = wordnet.getWord2();
		final OrthoWordIPAOptions word2opts = word2.getExtension(OrthoWordIPAOptions.class);
		
		final String selectedOpt = 
				(word1opts != null ? word1opts.getOptions().get(word1opts.getSelectedOption()) : "*")
				+ wordnet.getMarker().getMarker()
				+ (word2opts != null ? word2opts.getOptions().get(word2opts.getSelectedOption()) : "*");
		if(builder.size() > 0) builder.appendWordBoundary();
		builder.append(selectedOpt);
	}
	
	@Visits
	public void visitComment(OrthoComment comment) {
		final String commentTxt = comment.getData();
		if(commentTxt.matches("\\.{1,3}")) {
			if(builder.size() > 0) builder.appendWordBoundary();
			builder.append(commentTxt);
		}
	}
	
	public IPATranscript getTranscript() {
		return builder.toIPATranscript();
	}
}
