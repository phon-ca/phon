package ca.phon.ipa.phone.phonex;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import ca.phon.ipa.phone.Phone;

/**
 * TODO Document this
 *
 */
public class PhonexPattern {
	
	/**
	 * phonex pattern 
	 */
	private String pattern;
	
	/**
	 * The pattern as a fsa
	 */
	private PhonexFSA fsa;
	
	/**
	 * Compile the given phonex into
	 * a pattern
	 * 
	 * @param phonex the expression
	 * @throws PhonexPatternException if the expression's syntax is invalid
	 * 
	 */
	public static PhonexPattern compile(String phonex) {
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
			retVal.pattern = phonex;
			return retVal;
		} catch (RecognitionException e) {
			throw new PhonexPatternException(e);
		}
	}
	
	/**
	 * Hidden constructor
	 */
	public PhonexPattern(PhonexFSA fsa) {
		this.fsa = fsa;
	}
	
	/**
	 * Create a new phonex matcher which will match
	 * the given input against this pattern.
	 * 
	 * @param input The list of phones to be matched
	 */
	public PhonexMatcher matcher(Iterable<Phone> input) {
		List<Phone> tape = new ArrayList<Phone>();
		for(Phone p:input) tape.add(p);
		
		return new PhonexMatcher(this, tape);
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
	PhonexFSA getFsa() {
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
	 * @parma gIdx group index
	 * @return the group name for the specified group
	 *  or <code>null</code> if the group is not named
	 * @throws ArrayIndexOutOfBoundsException if the
	 *  given group index is out of bounds
	 */
	public String groupName(int gIdx) {
		return fsa.getGroupName(gIdx);
	}
}
