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

import ca.phon.phonexg4.PhonexLexer;
import ca.phon.phonexg4.PhonexParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.*;

import ca.phon.phonex.*;

/**
 * <p>Base matcher for all diacritic phonex plug-ins.  The matcher accepts
 * either a 
 * 
 */
public abstract class DiacriticPhoneMatcher implements PhoneMatcher {

	private PhoneMatcher matcher;
	
	public DiacriticPhoneMatcher(String phonex) {
		super();
		CharStream charStream = CharStreams.fromString(phonex);
		PhonexLexer lexer = new PhonexLexer(charStream);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		PhonexParser parser = new PhonexParser(tokenStream);

		ParseTree ctx = null;
		if(phonex.startsWith("[")) {
			ctx = parser.class_matcher();
		} else {
			ctx = parser.base_matcher();
		}
		PhonexCompiler2 compiler = new PhonexCompiler2();
		compiler.walkTree(ctx);
		matcher = compiler.getTopMatcher();
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
