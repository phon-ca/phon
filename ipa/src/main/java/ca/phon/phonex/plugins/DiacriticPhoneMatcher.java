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
package ca.phon.phonex.plugins;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.apache.logging.log4j.LogManager;

import ca.phon.phonex.PhoneMatcher;
import ca.phon.phonex.PhonexCompiler;
import ca.phon.phonex.PhonexLexer;
import ca.phon.phonex.PhonexParser;

/**
 * <p>Base matcher for all diacritic phonex plug-ins.  The matcher accepts
 * either a 
 * 
 */
public abstract class DiacriticPhoneMatcher implements PhoneMatcher {
	
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(DiacriticPhoneMatcher.class.getName());
	
	private PhoneMatcher matcher;
	
	public DiacriticPhoneMatcher(String phonex) {
		super();
		
		// compile phonex into a matcher
		CharStream exprStream = new ANTLRStringStream(phonex); 
		PhonexLexer lexer = new PhonexLexer(exprStream);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		PhonexParser parser = new PhonexParser(tokenStream);

		if(phonex.startsWith("[")) {
			try {
				PhonexParser.class_matcher_return val = parser.class_matcher();
				CommonTree cmTree = CommonTree.class.cast(val.getTree());
				CommonTreeNodeStream noes = new CommonTreeNodeStream(cmTree);
				PhonexCompiler compiler = new PhonexCompiler(noes);
				matcher = compiler.class_matcher();
			} catch (RecognitionException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		} else {	
			try {
				PhonexParser.single_phone_matcher_return val = parser.single_phone_matcher();
				CommonTree exprTree = CommonTree.class.cast(val.getTree());
				CommonTreeNodeStream noes = new CommonTreeNodeStream(exprTree);
				PhonexCompiler compiler = new PhonexCompiler(noes);
				matcher = compiler.single_phone_matcher();
			} catch (RecognitionException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
	}
	
	public DiacriticPhoneMatcher(PhoneMatcher matcher) {
		super();
		this.matcher = matcher;
	}

	public PhoneMatcher getMatcher() {
		return matcher;
	}

	public void setMatcher(PhoneMatcher matcher) {
		this.matcher = matcher;
	}

}
