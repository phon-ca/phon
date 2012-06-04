package ca.phon.ipa;

import java.util.ArrayList;
import java.util.Formattable;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;

import ca.phon.ipa.phone.BasicPhone;
import ca.phon.ipa.phone.CompoundPhone;
import ca.phon.ipa.phone.Phone;
import ca.phon.ipa.phone.parser.PhoneLexer;
import ca.phon.ipa.phone.parser.PhoneParser;
import ca.phon.ipa.phone.phonex.PhonexMatcher;
import ca.phon.ipa.phone.phonex.PhonexPattern;
import ca.phon.ipa.phone.phonex.PhonexPatternException;
import ca.phon.syllable.SyllableVisitor;
import ca.phon.visitor.Visitable;
import ca.phon.visitor.Visitor;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * 
 */
public final class IPATranscript extends ArrayList<Phone> implements Visitable<Phone> {
	
	/** Static logger */
	private final static Logger LOGGER = Logger.getLogger(IPATranscript.class
			.getName());

	private static final long serialVersionUID = 8942864962427274326L;
	
	/**
	 * Convert a string into an {@link IPATranscript}
	 * 
	 * @param transcription the text of the IPA transcription
	 * 
	 */
	public static IPATranscript parseTranscript(String transcript) {
		IPATranscript retVal = new IPATranscript();
		
		try {
			PhoneLexer lexer = new PhoneLexer(transcript);
			TokenStream tokenStream = new CommonTokenStream(lexer);
			PhoneParser parser = new PhoneParser(tokenStream);
			retVal = parser.transcription();
		} catch (RecognitionException re) {
			re.printStackTrace();
			LOGGER.warning(re.getMessage());
		}
		
		return retVal;
	}
	
	/**
	 * Create an empty transcript
	 */
	public IPATranscript() {
		super();
	}
	
	/**
	 * Create a new transcript for a list of phones.
	 */
	public IPATranscript(List<Phone> phones) {
		super(phones);
	}
	
	/**
	 * Returns <code>true</code> if this transcript matches
	 * the given phonex.
	 * 
	 * @param phonex
	 * @return <code>true</code> if the transcript matches
	 *  the given phonex, <code>false</code> otherwise
	 *  
	 * @throws PhonexPatternException if the given phonex
	 *  is not valid
	 */
	public boolean matches(String phonex) {
		boolean retVal = false;
		
		PhonexPattern pattern = PhonexPattern.compile(phonex);
		PhonexMatcher matcher = pattern.matcher(this);
		retVal = matcher.matches();
		
		return retVal;
	}
	
	/**
	 * Create a new transcript with all punctuation
	 * removed.
	 * 
	 * @return the filtered transcript
	 */
	public IPATranscript removePunctuation() {
		final IPATranscript retVal = new IPATranscript();
		final PunctuationFilter filter = new PunctuationFilter(retVal);
		accept(filter);
		return retVal;
	}
	
	/**
	 * Break the transcript into syllables.
	 * 
	 * @return syllables
	 */
	public List<IPATranscript> syllables() {
		final SyllableVisitor visitor = new SyllableVisitor();
		accept(visitor);
		return visitor.getSyllables();
	}

	@Override
	public void accept(Visitor<Phone> visitor) {
		// visit each phone in sequence
		for(Phone p:this) {
			visitor.visit(p);
		}
	}
	
	/**
	 * Phone visitor for filtering punctuation in transcriptions.
	 */
	public class PunctuationFilter extends VisitorAdapter<Phone> {
		
		/**
		 * filtered transcript
		 */
		private final IPATranscript transcript;
		
		public PunctuationFilter(IPATranscript t) {
			this.transcript = t;
		}

		@Override
		public void fallbackVisit(Phone obj) {
		}
		
		@Visits
		public void visitBasicPhone(BasicPhone phone) {
			transcript.add(phone);
		}
		
		@Visits
		public void visitCompoundPhone(CompoundPhone phone) {
			transcript.add(phone);
		}
	}
}
