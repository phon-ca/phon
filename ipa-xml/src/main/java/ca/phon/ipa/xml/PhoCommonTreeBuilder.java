package ca.phon.ipa.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

import ca.phon.ipa.CompoundPhone;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.Pause;
import ca.phon.ipa.PauseLength;
import ca.phon.ipa.Phone;
import ca.phon.ipa.StressMarker;
import ca.phon.ipa.StressType;
import ca.phon.ipa.SyllableBoundary;
import ca.phon.ipa.WordBoundary;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Visitor for building an ANTLR3 {@link CommonTree} from
 * an IPATranscript.
 *
 */
public class PhoCommonTreeBuilder extends VisitorAdapter<IPAElement> {
	
	private final static Logger LOGGER = Logger.getLogger(PhoCommonTreeBuilder.class.getName());
	
	private final static String PHO_START = "PHO";
	private final static String PW_START = "PW";
	private final static String PH_START = "PH";
	private final static String PH_LENGTH = "PH_LENGTH";
	private final static String PH_PREFIX = "PH_PREFIX";
	private final static String PH_SUFFIX = "PH_SUFFIX";
	private final static String PH_COMB = "PH_COMBINING";
	private final static String PH_TONE = "PH_TONE";
	private final static String CP_START = "CP";
	private final static String CP_LIG = "CP_LIG";
	private final static String SS_START = "SS";
	private final static String SS_TYPE = "SS_TYPE";
	private final static String SB_START = "SB";
	private final static String PAUSE_START = "PAUSE";
	private final static String PAUSE_LENGTH = "PAUSE_LENGTH";
	private final static String TEXT = "TEXT";
	
	private final static String PHO_TOKENS = "antlr3/tokens/Pho.tokens";
	
	/**
	 * Common tree
	 */
	private final CommonTree commonTree;
	
	// current word
	private CommonTree pwTree;
	
	// tokens
	private final Properties tokenProps = new Properties();
	
	/**
	 * Constructor
	 */
	public PhoCommonTreeBuilder() {
		final Token phoStart = new CommonToken(getTokenID(PHO_START));
		commonTree = new CommonTree(phoStart);
		final Token pwStart = new CommonToken(getTokenID(PW_START));
		pwTree = new CommonTree(pwStart);
		commonTree.addChild(pwTree);
	}
	
	private int getTokenID(String tokenName) {
		if(tokenProps.size() == 0) {
			final InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(PHO_TOKENS);
			if(is != null) {
				try {
					tokenProps.load(is);
				} catch (IOException e) {
					e.printStackTrace();
					LOGGER.warning(e.getMessage());
				}
			}
		}
		int retVal = -1;
		
		final String propVal = tokenProps.getProperty(tokenName);
		if(propVal != null) {
			try {
				retVal = Integer.parseInt(propVal);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				LOGGER.warning(nfe.getMessage());
			}
		}
		
		return retVal;
	}
	
	@Override
	public void fallbackVisit(IPAElement obj) {
	}
	
	@Visits
	public void visitCompoundPhone(CompoundPhone cp) {
		final CommonTree cpTree = new CommonTree(new CommonToken(getTokenID(CP_START)));
		
		if(cp.getLigature() == '\u0361') {
			final CommonToken ligToken = new CommonToken(getTokenID(CP_LIG), "top");
			cpTree.addChild(new CommonTree(ligToken));
		}
	
		final CommonTree p1Tree = createPhoneTree(cp.getFirstPhone());
		cpTree.addChild(p1Tree);
		
		final CommonTree p2Tree = createPhoneTree(cp.getSecondPhone());
		cpTree.addChild(p2Tree);
		
		pwTree.addChild(cpTree);
	}
	
	private CommonTree createPhoneTree(Phone p) {
		final CommonTree retVal = new CommonTree(new CommonToken(getTokenID(PH_START)));
		
		// setup attribute values
		if(p.getPrefix().length() > 0) {
			final CommonToken prefixToken = new CommonToken(getTokenID(PH_PREFIX), p.getPrefix());
			retVal.addChild(new CommonTree(prefixToken));
		}
		
		if(p.getSuffix().length() > 0) {
			final CommonToken suffixToken = new CommonToken(getTokenID(PH_SUFFIX), p.getSuffix());
			retVal.addChild(new CommonTree(suffixToken));
		}
		
		if(p.getLength() > 0.0f) {
			final String length = "" + p.getLength();
			final CommonToken lenToken = new CommonToken(getTokenID(PH_LENGTH), length);
			retVal.addChild(new CommonTree(lenToken));
		}
		
		if(p.getCombining().length() > 0) {
			final CommonToken combToken = new CommonToken(getTokenID(PH_COMB), p.getCombining());
			retVal.addChild(new CommonTree(combToken));
		}
		
		if(p.getTone().length() > 0) {
			final CommonToken toneToken = new CommonToken(getTokenID(PH_TONE), p.getTone());
			retVal.addChild(new CommonTree(toneToken));
		}
		
		// add text
		final CommonToken textToken = new CommonToken(getTokenID(TEXT), p.getBase());
		retVal.addChild(new CommonTree(textToken));
		
		return retVal;
	}
	
	@Visits
	public void visitPause(Pause p) {
		final CommonTree pauseTree = new CommonTree(new CommonToken(getTokenID(PAUSE_START)));
		
		if(p.getLength() != PauseLength.SHORT) {
			final CommonToken lenToken = new CommonToken(getTokenID(PAUSE_LENGTH), p.getLength().toString().toLowerCase());
			pauseTree.addChild(new CommonTree(lenToken));
		}
		
		pwTree.addChild(pauseTree);
	}
	
	@Visits
	public void visitPhone(Phone ph) {
		final CommonTree phTree = createPhoneTree(ph);
		pwTree.addChild(phTree);
	}
	
	@Visits
	public void visitStress(StressMarker ss) {
		final CommonTree ssTree = new CommonTree(new CommonToken(getTokenID(SS_START)));
		
		if(ss.getType() != StressType.PRIMARY) {
			final CommonToken typeToken = new CommonToken(getTokenID(SS_TYPE), "2");
			ssTree.addChild(new CommonTree(typeToken));
		}
		
		pwTree.addChild(ssTree);
	}
	
	@Visits
	public void visitSyllableBoundary(SyllableBoundary sb) {
		pwTree.addChild(new CommonTree(new CommonToken(getTokenID(SB_START))));
	}
	
	@Visits
	public void visitWordBoundary(WordBoundary wb) {
		pwTree = new CommonTree(new CommonToken(getTokenID(PW_START)));
		commonTree.addChild(pwTree);
	}
	
	public CommonTree getTree() {
		return this.commonTree;
	}
}
