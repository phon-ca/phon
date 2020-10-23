/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.ipalookup;

import ca.phon.ipa.*;
import ca.phon.orthography.*;
import ca.phon.visitor.*;
import ca.phon.visitor.annotation.*;

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
