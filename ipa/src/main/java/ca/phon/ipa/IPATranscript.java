package ca.phon.ipa;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;

import ca.phon.cvseq.CVSeqPattern;
import ca.phon.cvseq.CVSeqType;
import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.ipa.parser.IPALexer;
import ca.phon.ipa.parser.IPAParser;
import ca.phon.ipa.parser.exceptions.IPAParserException;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;
import ca.phon.phonex.PhonexPatternException;
import ca.phon.stresspattern.StressMatcherType;
import ca.phon.stresspattern.StressPattern;
import ca.phon.syllable.SyllabificationInfo;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.syllable.SyllableVisitor;
import ca.phon.util.Range;
import ca.phon.visitor.Visitable;
import ca.phon.visitor.Visitor;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * <p>A (somewhat) immutable representation of an IPA transcription.  While the number of elements
 * in the transcription cannot be changed, runtime extensions provided by the {@link IExtendable}
 * interface may be swapped.</p>
 * 
 * <p>Objects of this type should be created using either the {@link IPATranscript#parseIPATranscript(String)}
 * static method or {@link IPATranscriptBuilder}.</p>
 */
public final class IPATranscript implements Iterable<IPAElement>, Visitable<IPAElement>, IExtendable {
	
	/** Static logger */
	private final static Logger LOGGER = Logger.getLogger(IPATranscript.class
			.getName());

	private static final long serialVersionUID = 8942864962427274326L;
	
	private final ExtensionSupport extSupport = new ExtensionSupport(IPATranscript.class, this);
	
	private final IPAElement[] transcription;
	
	/**
	 * Convert a string into an {@link IPATranscript}
	 * 
	 * @param transcription the text of the IPA transcription
	 * 
	 */
	public static IPATranscript parseIPATranscript(String transcript) 
		throws ParseException {
		IPATranscript retVal = new IPATranscript();
		
		if(transcript.trim().length() > 0) {
			try {
				IPALexer lexer = new IPALexer(transcript);
				TokenStream tokenStream = new CommonTokenStream(lexer);
				
				IPAParser parser = new IPAParser(tokenStream);
				retVal = parser.transcription();
			} catch (RecognitionException re) {
				throw new ParseException(transcript, re.charPositionInLine);
			} catch (IPAParserException e) {
				final ParseException pe = new ParseException(e.getLocalizedMessage(), e.getPositionInLine());
//				pe.addSuppressed(e);
				throw pe;
			}
		}
		
		return retVal;
	}
	
	/**
	 * Create an empty transcript
	 */
	public IPATranscript() {
		this(new IPAElement[0]);
	}
	
	/**
	 * Create a new transcript for a list of phones.
	 */
	public IPATranscript(List<IPAElement> phones) {
		this(phones.toArray(new IPAElement[0]));
	}
	
	/**
	 * Createa  new transcript with the phones from the
	 * given transcript.
	 * 
	 * @param ipa
	 */
	public IPATranscript(IPATranscript ipa) {
		this(Arrays.copyOf(ipa.transcription, ipa.length()));
	}
	
	/**
	 * Create a new transcript from an array of phones.
	 */
	public IPATranscript(IPAElement ... phones) {
		super();
		this.transcription = phones;
		extSupport.initExtensions();
	}
	
	
	public IPATranscript(Object ...elements) {
		super();
		this.transcription = new IPAElement[elements.length];
		for(int i = 0; i < elements.length; i++) {
			final Object ele = elements[i];
			if(ele instanceof IPAElement) {
				transcription[i] = (IPAElement)ele;
			} else {
				transcription[i] = (new IPAElementFactory()).createPhone();
			}
		}
		extSupport.initExtensions();
	}
	
	/**
	 * Length of transcription (in elements)
	 * 
	 * @return length
	 */
	public int length() {
		return transcription.length;
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
	 * Get the element at specified index
	 * 
	 * @param index
	 * 
	 * @return element
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public IPAElement elementAt(int index) {
		return transcription[index];
	}
	
	/**
	 * Return the ipa element index of
	 * the given string index.
	 * 
	 * @param charIdx
	 * @return ipaIndex
	 * 
	 */
	public int ipaIndexOf(int charIdx) {
		int retVal = -1;
		
		final IPATranscriptBuilder builder = new IPATranscriptBuilder();
		for(int i = 0; i < length(); i++) {
			builder.append(elementAt(i));
			if(builder.toIPATranscript().toString().length() > charIdx) {
				retVal = i;
				break;
			}
		}
		
		return retVal;
	}
	
	/**
	 * Return the index of the given element
	 * 
	 * @param ele
	 * 
	 * @return index of element or < 0 if not found
	 */
	public int indexOf(IPAElement ele) {
		int retVal = -1;
		int idx = 0;
		for(IPAElement e:this) {
			if(ele == e) {
				retVal = idx;
				break;
			}
			++idx;
		}
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
		if(transcript == null || transcript.length() == 0) return -1;
		int retVal = -1;
		int idx = 0;
		for(IPAElement ele:this) {
			if(ele == transcript.elementAt(0)) {
				retVal = idx;
				break;
			}
			++idx;
		}
		
		if(retVal >= 0) {
			// test rest of transcript
			for(IPAElement ele:transcript) {
				if(idx >= length() || !(ele == elementAt(idx++))) {
					return -1;
				}
			}
			return retVal;
		}
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
					new IPATranscript(Arrays.copyOfRange(transcription, currentStart, matchStart));
			splitVals.add(splitValue);
			
			currentStart = matcher.end();
		}
		if(currentStart < this.length()) {
			final IPATranscript finalValue = 
					new IPATranscript(Arrays.copyOfRange(transcription, currentStart, transcription.length));
			splitVals.add(finalValue);
		}
		return splitVals.toArray(new IPATranscript[0]);
	}
	
	/**
	 * Return a subsection of this transcription.
	 * 
	 * @param start
	 * @param end
	 * 
	 * @return a new IPATranscript which is a sub-section of this transcription
	 * 
	 * @throws ArrayIndexOutOfBoundsException if either <code>start</code> or <code>end</code>
	 *  are out of bounds
	 */
	public IPATranscript subsection(int start, int end) {
		return new IPATranscript(Arrays.copyOfRange(transcription, start, end));
	}
	
	/**
	 * Return a new IPATranscript that include the contents
	 * of this transcript along with the contents of the given
	 * transcript appended at the end.
	 * 
	 * @param ipa
	 */
	public IPATranscript append(IPATranscript ipa) {
		final IPATranscriptBuilder builder = new IPATranscriptBuilder();
		return builder.append(this)
				.append(ipa)
				.toIPATranscript();
	}
	
	/**
	 * Create a new transcript with all punctuation
	 * removed.
	 * 
	 * @return the filtered transcript
	 */
	public IPATranscript removePunctuation() {
		final PunctuationFilter filter = new PunctuationFilter();
		accept(filter);
		return filter.getIPATranscript();
	}
	
	private List<IPATranscript> syllList = null;
	/**
	 * Break the transcript into syllables.
	 * 
	 * @return syllables
	 */
	public List<IPATranscript> syllables() {
		if(syllList == null) {
			final SyllableVisitor visitor = new SyllableVisitor();
			accept(visitor);
			syllList = Collections.unmodifiableList(visitor.getSyllables());
		}
		return syllList;
	}
	
	/**
	 * Reset syllabification for the transcript.
	 * 
	 */
	public void resetSyllabification() {
		final PunctuationFilter filter = new PunctuationFilter();
		accept(filter);
		for(IPAElement ele:filter.getIPATranscript()) {
			ele.setScType(SyllableConstituentType.UNKNOWN);
		}
		syllList = null;
	}
	
	private List<IPATranscript> wordList = null;
	/**
	 * Break the transcript into words
	 * 
	 * @return words
	 */
	public List<IPATranscript> words() {
		if(wordList == null) {
			final WordVisitor visitor = new WordVisitor();
			accept(visitor);
			wordList = visitor.getWords();
		}
		return wordList;
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
		if(transcript.length() > 0)
			return stringIndexOfElement(transcript.elementAt(0));
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
		if(index < 0 || index > length()) 
			throw new ArrayIndexOutOfBoundsException(index);
		final IPATranscript before =
				new IPATranscript(Arrays.copyOfRange(transcription, 0, index));
		return before.toString().length();
	}
	
	public String getStressPattern() {
		return StressPattern.getStressPattern(this.toList());
	}
	
	/**
	 * Does this transcript's stress pattern match the given
	 * {@link StressPattern}
	 * 
	 * @param pattern
	 * 
	 * @return <code>true</code> if pattern matches, <code>false</code> otherwise
	 */
	public boolean matchesStressPattern(String pattern) {
		boolean retVal = false;
		try {
			final StressPattern sp = StressPattern.compile(pattern);
			final String mySp = StressPattern.getStressPattern(this.toList());
			final List<StressMatcherType> stTypes = 
					StressMatcherType.toStressMatcherList(mySp);
			
			retVal = sp.matches(stTypes);
		} catch (ParseException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return retVal;
	}
	
	/**
	 * Does this transcript contain the given {@link StressPattern}
	 * 
	 * @param pattern
	 * @return <code>true</code> if this transcript contains the stress
	 *  pattern, <code>false</code> otherwise
	 */
	public boolean containsStressPattern(String pattern) {
		boolean retVal = false;
		try {
			final StressPattern sp = StressPattern.compile(pattern);
			final String mySp = StressPattern.getStressPattern(this.toList());
			final List<StressMatcherType> stTypes = 
					StressMatcherType.toStressMatcherList(mySp);
			
			retVal = sp.findWithin(stTypes);
		} catch (ParseException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return retVal;
	}
	
	/**
	 * Find all occurrences of the given {@link StressPattern}
	 * 
	 * @param pattern
	 * @return 
	 */
	public List<IPATranscript> findStressPattern(String pattern) {
		List<IPATranscript> retVal = new ArrayList<IPATranscript>();
		
		try {
			final StressPattern sp = StressPattern.compile(pattern);
			final String mySp = StressPattern.getStressPattern(this.toList());
			final List<StressMatcherType> stTypes = 
					StressMatcherType.toStressMatcherList(mySp);
			
			final List<Range> ranges = sp.findRanges(stTypes);
			for(Range range:ranges) {
				final Range phoneRange = 
						StressPattern.convertSPRToPR(this.toList(), mySp, range);
				final IPATranscript subT = subsection(phoneRange.getStart(), phoneRange.getEnd());
				retVal.add(subT);
			}
		} catch (ParseException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		return retVal;
	}
	
	public String getCvPattern() {
		return CVSeqPattern.getCVSeq(this.toList());
	}
	
	public boolean matchesCVPattern(String pattern) {
		boolean retVal = false;
		
		try {
			final CVSeqPattern cvPattern = CVSeqPattern.compile(pattern);
			final String myCVPattern = CVSeqPattern.getCVSeq(this.toList());
			final List<CVSeqType> cvTypes = CVSeqType.toCVSeqMatcherList(myCVPattern);
			
			retVal = cvPattern.matches(cvTypes);
		} catch (ParseException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		return retVal;
	}
	
	public boolean containsCVPattern(String pattern) {
		boolean retVal = false;
		
		try {
			final CVSeqPattern cvPattern = CVSeqPattern.compile(pattern);
			final String myCVPattern = CVSeqPattern.getCVSeq(this.toList());
			final List<CVSeqType> cvTypes = CVSeqType.toCVSeqMatcherList(myCVPattern);
			
			retVal = cvPattern.findWithin(cvTypes);
		} catch (ParseException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		return retVal;
	}
	
	public List<IPATranscript> findCVPattern(String pattern) {
		final List<IPATranscript> retVal = new ArrayList<IPATranscript>();
		
		try {
			final CVSeqPattern cvPattern = CVSeqPattern.compile(pattern);
			final String myCVPattern = CVSeqPattern.getCVSeq(this.toList());
			final List<CVSeqType> cvTypes = CVSeqType.toCVSeqMatcherList(myCVPattern);
			
			final List<Range> ranges = cvPattern.findRanges(cvTypes);
			for(Range range:ranges) {
				final Range phoneRange = 
						CVSeqPattern.convertCVRangeToPhoneRange(this.toList(), range);
				final IPATranscript subT = subsection(phoneRange.getStart(), phoneRange.getEnd());
				retVal.add(subT);
			}
		} catch (ParseException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		return retVal;
	}
	
	/**
	 * Phone visitor for filtering punctuation in transcriptions.
	 */
	public class PunctuationFilter extends VisitorAdapter<IPAElement> {
		
		/**
		 * filtered transcript
		 */
		private final IPATranscriptBuilder builder;
		
		public PunctuationFilter() {
			builder = new IPATranscriptBuilder();
		}

		@Override
		public void fallbackVisit(IPAElement obj) {
		}
		
		@Visits
		public void visitBasicPhone(Phone phone) {
			builder.append(phone);
		}
		
		@Visits
		public void visitCompoundPhone(CompoundPhone phone) {
			builder.append(phone);
		}
		
		public IPATranscript getIPATranscript() {
			return builder.toIPATranscript();
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
		return toString(false);
	}
	
	public String toString(final boolean includeScType) {
		final StringBuffer buffer = new StringBuffer();
		final Visitor<IPAElement> visitor = new Visitor<IPAElement>() {
			
			@Override
			public void visit(IPAElement obj) {
				buffer.append(obj.toString());
				if(includeScType && obj.getScType() != SyllableConstituentType.WORDBOUNDARYMARKER) {
					buffer.append(":");
					final SyllabificationInfo sInfo = obj.getExtension(SyllabificationInfo.class);
					if(sInfo.getConstituentType() == SyllableConstituentType.NUCLEUS && sInfo.isDiphthongMember())
						buffer.append("D");
					else
						buffer.append(sInfo.getConstituentType().getIdChar());
				}
			}
		};
		accept(visitor);
		return buffer.toString();
	}
	
	/**
	 * Get an immutable list representation of this
	 * IPATranscript.
	 * 
	 * @return list
	 */
	public List<IPAElement> toList() {
		return Collections.unmodifiableList(Arrays.asList(transcription));
	}

	@Override
	public Iterator<IPAElement> iterator() {
		return toList().iterator();
	}
	
}
