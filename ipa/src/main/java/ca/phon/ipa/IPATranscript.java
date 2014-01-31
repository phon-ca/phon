package ca.phon.ipa;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
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
public final class IPATranscript extends ArrayList<IPAElement> implements Visitable<IPAElement>, IExtendable {
	
	/** Static logger */
	private final static Logger LOGGER = Logger.getLogger(IPATranscript.class
			.getName());

	private static final long serialVersionUID = 8942864962427274326L;
	
	private final ExtensionSupport extSupport = new ExtensionSupport(IPATranscript.class, this);
	
	/**
	 * Convert a string into an {@link IPATranscript}
	 * 
	 * @param transcription the text of the IPA transcription
	 * 
	 */
	public static IPATranscript parseIPATranscript(String transcript) 
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
		this(new ArrayList<IPAElement>());
	}
	
	/**
	 * Create a new transcript for a list of phones.
	 */
	public IPATranscript(List<IPAElement> phones) {
		super(phones);
		extSupport.initExtensions();
	}
	
	/**
	 * Create a new transcript from an array of phones.
	 */
	public IPATranscript(IPAElement ... phones) {
		super(Arrays.asList(phones));
		extSupport.initExtensions();
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
	 * Returns <code>true</code> if this transcript contains
	 * the given phonex pattern.
	 * 
	 * @param phonex
	 * @return <code>true</code> if the transcript contains
	 *  the given phonex pattern, <code>false</code> otherwise
	 *  
	 * @throws PhonexPatternException if the given phonex
	 *  is not valid
	 */
	public boolean contains(String phonex) {
		boolean retVal = false;
		
		PhonexPattern pattern = PhonexPattern.compile(phonex);
		PhonexMatcher matcher = pattern.matcher(this);
		retVal = matcher.find();
		
		return retVal;
	}
	
	/**
	 * Returns the index of the first phone of the given phonex pattern
	 * in this transcript.
	 * 
	 * @param phonex
	 * 
	 * @return the index of the first phone in the given pattern or
	 *  -1 if not found
	 *  
	 *  @throws PhonexPatternException if the given phonex
	 *   is not valid
	 */
	public int indexOf(String phonex) {
		int retVal = -1;
		
		PhonexPattern pattern = PhonexPattern.compile(phonex);
		PhonexMatcher matcher = pattern.matcher(this);
		
		if(matcher.find()) {
			retVal = matcher.start();
		}
		
		return retVal;
	}
	
	/**
	 * Returns the index of the first phone of the given phonex pattern
	 * in this transcript starting at the given phone index.
	 * 
	 * @param phonex
	 * @param index of the phone to start searching from
	 * 
	 * @return the index of the first phone in the given pattern or
	 *  -1 if not found
	 *  
	 *  @throws PhonexPatternException if the given phonex
	 *   is not valid
	 */
	public int indexOf(String phonex, int index) {
		int retVal = -1;
		
		PhonexPattern pattern = PhonexPattern.compile(phonex);
		PhonexMatcher matcher = pattern.matcher(this);
		
		if(matcher.find(index)) {
			retVal = matcher.start();
		}
		
		return retVal;
	}
	
	/**
	 * Return the index of the first element in the given
	 * transcript.
	 * 
	 * @param transcript
	 * @return the index or -1 if not found
	 */
	public int indexOf(IPATranscript transcript) {
		if(transcript.size() > 0) 
			return super.indexOf(transcript.get(0));
		else
			return -1;
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
	
	/**
	 * Break the transcript into words
	 * 
	 * @return words
	 */
	public List<IPATranscript> words() {
		final WordVisitor visitor = new WordVisitor();
		accept(visitor);
		return visitor.getWords();
	}

	@Override
	public void accept(Visitor<IPAElement> visitor) {
		// visit each phone in sequence
		for(IPAElement p:this) {
			visitor.visit(p);
		}
	}
	
	/**
	 * Finds the index of the given ipa element in
	 * the string representation of the transcript.
	 * 
	 * @param element
	 * @return the string index of the specified element
	 *  or < 0 if not found
	 */
	public int stringIndexOfElement(IPAElement ele) {
		final int eleIdx = indexOf(ele);
		if(eleIdx >= 0) {
			return stringIndexOfElement(eleIdx);
		} else {
			return eleIdx;
		}
	}
	
	/**
	 * Find the index of the given ipa transcript in 
	 * the string representation of this transcript.
	 * 
	 * @param transcript
	 * @return the string index of the given transcript
	 *  or < 0 if not found
	 */
	public int stringIndexOf(IPATranscript transcript) {
		if(transcript.size() > 0)
			return stringIndexOfElement(transcript.get(0));
		else
			return -1;
	}
	
	/**
	 * Finds the index of the specified ipa element in
	 * the string representation of the transcript.
	 * 
	 * @param index
	 * @return the string index of the specified element
	 *  or < 0 if not found
	 */
	public int stringIndexOfElement(int index) {
		if(index < 0 || index >= size()) 
			throw new ArrayIndexOutOfBoundsException(index);
		final IPATranscript before =
				new IPATranscript(super.subList(0, index));
		return before.toString().length();
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

	@Override
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}
	
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		final Visitor<IPAElement> visitor = new Visitor<IPAElement>() {
			@Override
			public void visit(IPAElement obj) {
				buffer.append(obj.toString());
			}
		};
		accept(visitor);
		return buffer.toString();
	}
}
