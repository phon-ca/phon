/*
 * Copyright (C) Gregory Hedlund & Yvan Rose
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
package ca.phon.phonex;

import java.util.*;
import java.util.BitSet;

import ca.phon.util.PrefHelper;
import org.antlr.runtime.*;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.*;

import ca.phon.ipa.*;
import ca.phon.syllable.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * <p>A compiled representation of a phonex expression.</p>
 *
 * <p>A phonex expression, specified as a string, must first be compiled
 * into this class using the {@link #compile(String)} method.  The
 * resulting {@link PhonexPattern} object can then be used to create
 * a {@link PhonexMatcher} that can match arbitrary IPA sequences
 * against the phonex expression.  A single pattern can be used to
 * create many matchers.</p>
 *
 * <p>A typcial use case:
 *
 * <pre>
 * // an expression to find geminates
 * final String phonex = "(\c)\S?\1";
 * final PhonexPattern pattern = PhonexPattern.compile(phonex);
 *
 * final IPATranscript ipa = IPATranscript.parseIPATranscript("ˈlæmp.poʊst");
 * final PhonexMatcher matcher = pattern.matcher(ipa);
 * boolean b = matcher.find();
 * </pre></p>
 *
 * <p>For more information about phonex expressions, please
 * refer to the phonex manual.</p>
 *
 */
public class PhonexPattern implements Comparable<PhonexPattern> {

	public final static String USE_ANTLR4 = PhonexPattern.class.getName() + ".useAntlr4";
	private final static boolean defaultUseAntlr4 = true;

	/**
	 * phonex pattern
	 */
	private String pattern;

	/**
	 * The pattern as a fsa
	 */
	private PhonexFSA fsa;

	private int flags = 0;

	/**
	 * Compile a phonex expression for a single matcher (including phone classes)
	 * and return the provided matcher.
	 *
	 * @param phonex
	 * @return
	 * @throws PhonexPatternException
	 */
	public static PhoneMatcher compileSingleMatcher(String phonex) throws PhonexPatternException {
		ErrorListener listener = new ErrorListener();

		org.antlr.v4.runtime.CharStream charStream = CharStreams.fromString(phonex);
		ca.phon.phonexg4.PhonexLexer lexer = new ca.phon.phonexg4.PhonexLexer(charStream);
		lexer.addErrorListener(listener);
		org.antlr.v4.runtime.CommonTokenStream tokenStream = new org.antlr.v4.runtime.CommonTokenStream(lexer);
		ca.phon.phonexg4.PhonexParser parser = new ca.phon.phonexg4.PhonexParser(tokenStream);
		parser.addErrorListener(listener);
		PhonexCompiler2 compiler = new PhonexCompiler2();

		ParseTree ctx = null;
		if(phonex.startsWith("[")) {
			ctx = parser.class_matcher();
		} else {
			ctx = parser.base_matcher();
		}
		if(!listener.exceptions.isEmpty())
			throw listener.exceptions.get(0);

		compiler.walkTree(ctx);
		return compiler.getTopMatcher();
	}

	public static PhonexPattern compile(String phonex) throws PhonexPatternException {
		return compile(phonex, 0);
	}

	/**
	 * Compile the given phonex into
	 * a pattern
	 *
	 * @param phonex the expression
	 * @param flags
	 * @throws PhonexPatternException if the expression's syntax is invalid
	 *
	 */
	public static PhonexPattern compile(String phonex, int flags) throws PhonexPatternException {
		if(PrefHelper.getBoolean(USE_ANTLR4, defaultUseAntlr4)) {
			return compileAntlr4(phonex, flags);
		} else {
			return compileAntlr3(phonex, flags);
		}
	}

	private static PhonexPattern compileAntlr4(String phonex, int flags) throws PhonexPatternException {
		ErrorListener listener = new ErrorListener();

		org.antlr.v4.runtime.CharStream charStream = CharStreams.fromString(phonex);
		ca.phon.phonexg4.PhonexLexer lexer = new ca.phon.phonexg4.PhonexLexer(charStream);
		lexer.addErrorListener(listener);
		org.antlr.v4.runtime.CommonTokenStream tokenStream = new org.antlr.v4.runtime.CommonTokenStream(lexer);

		ca.phon.phonexg4.PhonexParser parser = new ca.phon.phonexg4.PhonexParser(tokenStream);
		parser.addErrorListener(listener);
		PhonexCompiler2 compiler = new PhonexCompiler2();

		ca.phon.phonexg4.PhonexParser.ExprContext exprContext = parser.expr();
		if(listener.exceptions.size() > 0)
			throw listener.exceptions.get(0);

		compiler.walkTree(exprContext);

		PhonexPattern retVal = new PhonexPattern(compiler.getFsa());
		retVal.pattern = phonex;
		retVal.flags = flags | compiler.flags;
		return retVal;
	}

	private static PhonexPattern compileAntlr3(String phonex, int flags) throws PhonexPatternException {
		CharStream exprStream = new ANTLRStringStream(phonex);
		PhonexLexer lexer = new PhonexLexer(exprStream);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		PhonexParser parser = new PhonexParser(tokenStream);

		PhonexParser.expr_return exprVal = null;

		try {
			exprVal = parser.expr();

			CommonTree exprTree = CommonTree.class.cast(exprVal.getTree());
			CommonTreeNodeStream noes = new CommonTreeNodeStream(exprTree);
			PhonexCompiler compiler = new PhonexCompiler(noes);
			PhonexFSA fsa = compiler.expr();

			PhonexPattern retVal = new PhonexPattern(fsa);
			retVal.flags = flags | compiler.getFlags();
			retVal.pattern = phonex;
			return retVal;
		} catch (RecognitionException e) {
			throw new PhonexPatternException(e.line, e.charPositionInLine, e);
		} catch (RewriteEarlyExitException e) {
			throw new PhonexPatternException(0, -1, e);
		}
	}

	/**
	 * constructor
	 *
	 * @param fsa
	 */
	PhonexPattern(PhonexFSA fsa) {
		this.fsa = fsa;
	}

	/**
	 * Create a new phonex matcher which will match
	 * the given input against this pattern.
	 *
	 * @param input The list of phones to be matched
	 */
	public PhonexMatcher matcher(Iterable<IPAElement> input) {
		final IPATranscriptBuilder builder = new IPATranscriptBuilder();
		for(IPAElement ele:input) builder.append(ele);
		IPATranscript transcript = builder.toIPATranscript();

		SyllabificationInfo.setupSyllabificationInfo(transcript);
		List<IPAElement> tape = transcript.toList();

		return new PhonexMatcher(this, tape, flags);
	}

	/**
	 * Return the phonex for this pattern.
	 *
	 * @return the phonex compiled into this
	 *  pattern
	 */
	public String pattern() {
		return this.pattern;
	}

	/**
	 * Access the internal fsa for package-level classes.
	 *
	 * @return the complied fsa
	 */
	public PhonexFSA getFsa() {
		return this.fsa;
	}

	/**
	 * Returns the number of groups in the
	 * compiled pattern.
	 *
	 * @return the number of groups in the pattern
	 */
	public int numberOfGroups() {
		int retVal = 0;
		if(fsa != null)
			retVal = fsa.getNumberOfGroups();
		return retVal;
	}

	/**
	 * Get the group name for the specified group
	 *
	 * @oaram gIdx group index
	 * @return the group name for the specified group
	 *  or <code>null</code> if the group is not named
	 * @throws ArrayIndexOutOfBoundsException if the
	 *  given group index is out of bounds
	 */
	public String groupName(int gIdx) {
		return fsa.getGroupName(gIdx);
	}

	/**
	 * Return the group index for the given group name.
	 *
	 * @return group index or < 0 if not found
	 */
	public int groupIndex(String groupName) {
		for(int i = 1; i <= numberOfGroups(); i++) {
			if(groupName(i) != null && groupName(i).equals(groupName)) return i;
		}
		return -1;
	}

	/**
	 * Compares phonex pattern strings.
	 */
	@Override
	public int compareTo(PhonexPattern arg0) {
		return pattern().compareTo(arg0.pattern());
	}

	private static class ErrorListener implements ANTLRErrorListener {

		List<PhonexPatternException> exceptions = new ArrayList<>();

		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, org.antlr.v4.runtime.RecognitionException e) {
			exceptions.add(new PhonexPatternException(line, charPositionInLine, e));
		}

		@Override
		public void reportAmbiguity(org.antlr.v4.runtime.Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {}

		@Override
		public void reportAttemptingFullContext(org.antlr.v4.runtime.Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {}

		@Override
		public void reportContextSensitivity(org.antlr.v4.runtime.Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {}

	}

}
