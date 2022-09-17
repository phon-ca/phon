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
package ca.phon.app.ipalookup;

import ca.phon.ipadictionary.IPADictionary;
import ca.phon.orthography.*;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.util.*;
import java.util.regex.*;

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
	
	
	private String stripTrailingPunctuation(String ortho) {
		final Pattern p = Pattern.compile(".+(\\p{Punct}+)");
		final Matcher m = p.matcher(ortho);
		
		if(m.matches()) {
			int pIdx = m.start(1);
			return ortho.substring(0, pIdx);
		} else {
			return ortho;
		}
	}
	
	private String[] lookup(String ortho) {
		ortho = stripTrailingPunctuation(ortho);
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
			
			String opts[];
			if("xxx".contentEquals(text) || "yyy".contentEquals(text)
					|| "www".contains(text) || "*".contentEquals(text)) {
				opts = new String[] { "*" };
			} else {
				opts = lookup(text);
			}
			
			ext = new OrthoWordIPAOptions(opts);
			ext.setDictLang(dictionary.getLanguage());
			if(opts.length > 0) ext.setSelectedOption(0);
			word.putExtension(OrthoWordIPAOptions.class, ext);
		}
		return ext;
	}
	
}