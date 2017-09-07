/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.session.editor.view.ipa_lookup;

import ca.phon.app.ipalookup.OrthoWordIPAOptions;
import ca.phon.ipa.*;
import ca.phon.orthography.*;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

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
