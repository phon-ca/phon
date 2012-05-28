package ca.phon.ipa.phonex;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import ca.phon.fsa.FSAState;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.phone.Phone;
import ca.phon.ipa.phone.phonex.PhonexCompiler;
import ca.phon.ipa.phone.phonex.PhonexFSA;
import ca.phon.ipa.phone.phonex.PhonexLexer;
import ca.phon.ipa.phone.phonex.PhonexParser;

import junit.framework.TestCase;
import junit.framework.TestFailure;

/**
 * Test basic parsing abilities of the phonex parser.
 */
public class TestPhonexParser extends TestCase {
	
	/**
	 * Test feature set matchers
	 */
	public void testFeatureSetMatcher() {
		String[] exprs = {
				".*\\c",
				"t_{fricative}.*",
				"^h.*o$",
				"\\bh(.*o)\\b",
				"[\\c\\v]+",
				"\\u0068\\v(\\c+\\v)",
				"\\w+",
				"(cv1=he)(cvc=((\\c)\\4)\\v)",
				"({c}+{v})<2>",
				"(?=({c}*{v})+)",
				"(one=\\c(?=\\v))\\c(two=\\c+\\v)",
				"'\u0068'\\v(\\c+)\\v"
//				"({C}{V})+{G}?",
//				"{}:LA*{}:O*{}:N+{}:C*{}:RA*",
//				"({}:LA)*({}:O)*({}:N)+({}:C)*({}:RA)*",
//				"({}:LA*)({}:O*)({}:N+)({}:C*)({}:RA*)"
		};
		
		
		int tIdx = 0;
		for(String expr:exprs) {
			System.out.println(expr);
			assertEquals(true, performParseTest(expr, tIdx++));
		}
	}
	
	/**
	 * Perform a parsing test on the given string
	 * 
	 * @param expr
	 */
	private boolean performParseTest(String expr, int testIdx) {
//		boolean retVal =
		CharStream exprStream = new ANTLRStringStream(expr); 
		PhonexLexer lexer = new PhonexLexer(exprStream);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		PhonexParser parser = new PhonexParser(tokenStream);
		
		PhonexParser.expr_return exprVal = null;
		
		IPATranscript transcript = IPATranscript.fromString("t͜ʃemɪarst");
		
		try {
			exprVal = parser.expr();
			
			CommonTree exprTree = CommonTree.class.cast(exprVal.getTree());
			CommonTreeNodeStream noes = new CommonTreeNodeStream(exprTree);
			PhonexCompiler compiler = new PhonexCompiler(noes);
			PhonexFSA fsa = compiler.expr();
			
			FSAState<Phone> lastState = 
					fsa.runWithTape(transcript.toArray(new Phone[0]));
			
			if(fsa.isFinalState(lastState.getCurrentState())) {
				for(int i = 0; i <= fsa.getNumberOfGroups(); i++) {
					Phone[] grp = lastState.getGroup(i);
					String grpName = fsa.getGroupName(i);
					System.out.print((grpName != null ? grpName : i) + ":");
					for(Phone p:grp) {
						System.out.print(p);
					}
					System.out.println();
				}
			}
			
			PrintWriter out = new PrintWriter(new File("/Users/ghedlund/test" + expr + ".dot"));
			out.println(fsa.getDotText());
			out.flush();
			out.close();

			return true;
			
			
		} catch (RecognitionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
