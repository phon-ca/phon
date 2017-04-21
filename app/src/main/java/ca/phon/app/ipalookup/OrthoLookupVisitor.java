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
package ca.phon.app.ipalookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ca.phon.ipadictionary.IPADictionary;
import ca.phon.orthography.OrthoComment;
import ca.phon.orthography.OrthoElement;
import ca.phon.orthography.OrthoWord;
import ca.phon.orthography.OrthoWordnet;
import ca.phon.orthography.Orthography;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Produce a set of suggested transcriptions for a given {@link Orthography}.
 */
public class OrthoLookupVisitor extends VisitorAdapter<OrthoElement> {
	
	private final IPADictionary dictionary;
	
	public OrthoLookupVisitor(IPADictionary dict) {
		this.dictionary = dict;
	}
	
	@Override
	public void visit(OrthoElement ele) {
		if(dictionary != null)
			super.visit(ele);
	}
	
	@Override
	public void fallbackVisit(OrthoElement obj) {
	}
	
	@Visits
	public void visitWord(OrthoWord word) {
		updateAnnotation(word);
	}
	
	@Visits
	public void visitWordnet(OrthoWordnet wordnet) {
		final OrthoWord word1 = wordnet.getWord1();
		visitWord(word1);
		final OrthoWord word2 = wordnet.getWord2();
		visitWord(word2);
		
		// create option combos
		final OrthoWordIPAOptions word1Ext = word1.getExtension(OrthoWordIPAOptions.class);
		final List<String> word1Opts = (word1Ext.getOptions().size() > 0 ? word1Ext.getOptions() : Arrays.asList(new String[]{"*"}));
		final OrthoWordIPAOptions word2Ext = word2.getExtension(OrthoWordIPAOptions.class);
		final List<String> word2Opts = (word2Ext.getOptions().size() > 0 ? word2Ext.getOptions() : Arrays.asList(new String[]{"*"}));
		
		final List<String> wordnetOpts = new ArrayList<String>();
		for(String word1Opt:word1Opts) {
			for(String word2Opt:word2Opts) {
				wordnetOpts.add(
						word1Opt + wordnet.getMarker().getMarker() + word2Opt);
			}
		}
		final OrthoWordIPAOptions wordnetExt = new OrthoWordIPAOptions();
		wordnetExt.setDictLang(dictionary.getLanguage());
		wordnetExt.setOptions(wordnetOpts);
		wordnet.putExtension(OrthoWordIPAOptions.class, wordnetExt);
	}
	
	private String[] lookup(String ortho) {
		String[] retVal = dictionary.lookup(ortho);
		if(retVal.length == 0)
			retVal = new String[]{ "*" };
		return retVal;
	}
	
	@Visits
	public void visitComment(OrthoComment comment) {
		// check for pauses
		final String commentTxt = comment.getData();
		if(commentTxt.matches("\\.{1,3}")) {
			final OrthoWordIPAOptions opts = new OrthoWordIPAOptions(Collections.singletonList("(" + commentTxt + ")"));
			opts.setDictLang(dictionary.getLanguage());
			opts.setSelectedOption(0);
			comment.putExtension(OrthoWordIPAOptions.class, opts);
		}
	}
	
	private OrthoWordIPAOptions updateAnnotation(OrthoWord word) {
		OrthoWordIPAOptions ext = word.getExtension(OrthoWordIPAOptions.class);
		if(ext == null || ext.getDictLang() != dictionary.getLanguage()) {
			String text = word.getWord();
			
			String[] opts = lookup(text);
			ext = new OrthoWordIPAOptions(opts);
			ext.setDictLang(dictionary.getLanguage());
			if(opts.length > 0) ext.setSelectedOption(0);
			word.putExtension(OrthoWordIPAOptions.class, ext);
		}
		return ext;
	}
	
}