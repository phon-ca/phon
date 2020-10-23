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
package ca.phon.app.session.editor.view.ipa_lookup;

import ca.phon.app.ipalookup.*;
import ca.phon.ipa.*;
import ca.phon.orthography.*;
import ca.phon.visitor.*;
import ca.phon.visitor.annotation.*;

public class WordLookupVisitor extends VisitorAdapter<OrthoElement> {

	/**
	 * 
	 */
	private final RecordLookupPanel recordLookupPanel;

	/**
	 * @param recordLookupPanel
	 */
	WordLookupVisitor(RecordLookupPanel recordLookupPanel) {
		this.recordLookupPanel = recordLookupPanel;
	}

	@Override
	public void fallbackVisit(OrthoElement obj) {
	}
	
	@Visits
	public void visitWord(OrthoWord word) {
		OrthoWordIPAOptions ext = word.getExtension(OrthoWordIPAOptions.class);
		if(ext == null) ext = new OrthoWordIPAOptions();
		
		final String txt = (ext.getOptions().size() > 0 ? ext.getOptions().get(ext.getSelectedOption()) : "*");
		addWordToTier(txt);
	}
	
	@Visits
	public void visitCompoundWord(OrthoWordnet wordnet) {
		OrthoWordIPAOptions opt1 = wordnet.getWord1().getExtension(OrthoWordIPAOptions.class);
		if(opt1 == null) opt1 = new OrthoWordIPAOptions();
		OrthoWordIPAOptions opt2 = wordnet.getWord2().getExtension(OrthoWordIPAOptions.class);
		if(opt2 == null) opt2 = new OrthoWordIPAOptions();
		
		final String t1 = (opt1.getOptions().size() > 0 ? opt1.getOptions().get(opt1.getSelectedOption()) : "*");
		final String t2 = (opt2.getOptions().size() > 0 ? opt2.getOptions().get(opt2.getSelectedOption()) : "*");
		addWordToTier(t1 + wordnet.getMarker().toString() + t2);
	}
	
	@Visits
	public void visitComment(OrthoComment comment) {
		if(comment.getData().matches("\\.{1,3}")) {
			OrthoWordIPAOptions ext = comment.getExtension(OrthoWordIPAOptions.class);
			if(ext == null) ext = new OrthoWordIPAOptions();
			
			final String txt = (ext.getOptions().size() > 0 ? ext.getOptions().get(ext.getSelectedOption()) 
					: "(" + comment.getData() + ")");
			addWordToTier(txt);
		}
	}

	private void addWordToTier(String txt) {
		int grpIdx = recordLookupPanel.lookupTier.numberOfGroups()-1;
		IPATranscript grp = recordLookupPanel.lookupTier.getGroup(grpIdx);
		final IPATranscriptBuilder builder = new IPATranscriptBuilder();
		builder.append(grp);
		if(builder.size() > 0)
			builder.appendWordBoundary();
		builder.append(txt);
		recordLookupPanel.lookupTier.setGroup(grpIdx, builder.toIPATranscript());
	}

}
