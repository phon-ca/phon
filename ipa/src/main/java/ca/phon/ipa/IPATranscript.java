package ca.phon.ipa;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Formattable;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;

import ca.phon.ipa.parser.IPALexer;
import ca.phon.ipa.parser.IPAParser;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;
import ca.phon.phonex.PhonexPatternException;
import ca.phon.syllable.SyllableVisitor;
import ca.phon.visitor.Visitable;
import ca.phon.visitor.Visitor;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * 
 */
public final class IPATranscript extends ArrayList<IPAElement> implements Visitable<IPAElement> {
	
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
	public static IPATranscript parseTranscript(String transcript) 
		throws ParseException {
		IPATranscript retVal = new IPATranscript();
		
		try {
			IPALexer lexer = new IPALexer(transcript);
			TokenStream tokenStream = new CommonTokenStream(lexer);
			IPAParser parser = new IPAParser(tokenStream);
			retVal = parser.transcription();
		} catch (RecognitionException re) {
			throw new ParseException(transcript, re.charPositionInLine);
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
	public IPATranscript(List<IPAElement> phones) {
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
	 * Split transcript using the given phonex pattern
	 * as a delimiter.
	 * 
	 * @param phonex
	 */
	public IPATranscript[] split(String phonex) {
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(this);
		
		final List<IPATranscript> splitVals = new ArrayList<IPATranscript>();
		int currentStart = 0;
		while(matcher.find()) {
			final int matchStart = matcher.start();
			
			if(currentStart == 0 && matchStart == 0)
				continue;
			
			final IPATranscript splitValue = 
					new IPATranscript(this.subList(currentStart, matchStart));
			splitVals.add(splitValue);
			
			currentStart = matcher.end();
		}
		if(currentStart < this.size()) {
			final IPATranscript finalValue = 
					new IPATranscript(this.subList(currentStart, this.size()));
			splitVals.add(finalValue);
		}
		return splitVals.toArray(new IPATranscript[0]);
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
	public void accept(Visitor<IPAElement> visitor) {
		// visit each phone in sequence
		for(IPAElement p:this) {
			visitor.visit(p);
		}
	}
	
	/**
	 * Phone visitor for filtering punctuation in transcriptions.
	 */
	public class PunctuationFilter extends VisitorAdapter<IPAElement> {
		
		/**
		 * filtered transcript
		 */
		private final IPATranscript transcript;
		
		public PunctuationFilter(IPATranscript t) {
			this.transcript = t;
		}

		@Override
		public void fallbackVisit(IPAElement obj) {
		}
		
		@Visits
		public void visitBasicPhone(Phone phone) {
			transcript.add(phone);
		}
		
		@Visits
		public void visitCompoundPhone(CompoundPhone phone) {
			transcript.add(phone);
		}
	}
}
