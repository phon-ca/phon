package ca.phon.phonex.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import ca.phon.phonex.PhoneMatcher;
import ca.phon.phonex.PhonexCompiler;
import ca.phon.phonex.PhonexFSA;
import ca.phon.phonex.PhonexLexer;
import ca.phon.phonex.PhonexParser;

/**
 * <p>Base matcher for all diacritic phonex plug-ins.  The matcher accepts
 * either a 
 * 
 */
public abstract class DiacriticPhoneMatcher implements PhoneMatcher {
	
	private static final Logger LOGGER = Logger
			.getLogger(DiacriticPhoneMatcher.class.getName());
	
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
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		} else {	
			try {
				PhonexParser.single_phone_matcher_return val = parser.single_phone_matcher();
				CommonTree exprTree = CommonTree.class.cast(val.getTree());
				CommonTreeNodeStream noes = new CommonTreeNodeStream(exprTree);
				PhonexCompiler compiler = new PhonexCompiler(noes);
				matcher = compiler.single_phone_matcher();
			} catch (RecognitionException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
