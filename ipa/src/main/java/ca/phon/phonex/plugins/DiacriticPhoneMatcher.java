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
package ca.phon.phonex.plugins;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.apache.logging.log4j.*;

import ca.phon.phonex.*;

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
