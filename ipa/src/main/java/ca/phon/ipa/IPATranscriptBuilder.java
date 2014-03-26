package ca.phon.ipa;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for building {@link IPATranscript}s.
 */
public class IPATranscriptBuilder {
	
	private final static Logger LOGGER = Logger
			.getLogger(IPATranscriptBuilder.class.getName());
	
	private final IPAElementFactory factory = new IPAElementFactory();

	/**
	 * Element buffer
	 */
	private final List<IPAElement> buffer = new ArrayList<IPAElement>();
	
	public IPATranscriptBuilder() {
		
	}
	
	/**
	 * Append the given {@link IPAElement}
	 * 
	 * @param ipaElement
	 * 
	 * @return builder
	 */
	public IPATranscriptBuilder append(IPAElement ipaElement) {
		buffer.add(ipaElement);
		return this;
	}
	
	/**
	 * Append all elements
	 * 
	 * @param eleList
	 * 
	 * @return builder
	 */
	public IPATranscriptBuilder append(Collection<? extends IPAElement> eleList) {
		buffer.addAll(eleList);
		return this;
	}
	
	/**
	 * Append the given string to the transcript.
	 * This utilizes the {@link IPATranscript#parseIPATranscript(String)} method.
	 * 
	 * @param ipa
	 * 
	 * @return builder
	 */
	public IPATranscriptBuilder append(String ipa) {
		try {
			final IPATranscript transcript = IPATranscript.parseIPATranscript(ipa);
			append(transcript);
		} catch (ParseException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return this;
	}
	
	/**
	 * Append the given transcript
	 * 
	 * @param ipa
	 * 
	 * @return builder
	 */
	public IPATranscriptBuilder append(IPATranscript ipa) {
		for(IPAElement ele:ipa) {
			append(ele);
		}
		return this;
	}
	
	/**
	 * Append a word boundary.
	 * 
	 * @return builder
	 */
	public IPATranscriptBuilder appendWordBoundary() {
		append(factory.createWordBoundary());
		return this;
	}
	
	/**
	 * Append a syllable boundary (e.g., '.')
	 * 
	 * @return builder
	 */
	public IPATranscriptBuilder appendSyllableBoundary() {
		append(factory.createSyllableBoundary());
		return this;
	}
	
	/**
	 * Append a compound word marker (e.g, '+')
	 * 
	 * @return builder
	 */
	public IPATranscriptBuilder appendCompoundWordMarker() {
		append(factory.createCompoundWordMarker());
		return this;
	}
	
	/**
	 * Append a linker (including liason)
	 * @return builder
	 */
	public IPATranscriptBuilder appendLinker() {
		append(factory.createLinker());
		return this;
	}
	
	/**
	 * Append contraction
	 * 
	 * @return builder
	 */
	public IPATranscriptBuilder appendContraction() {
		append(factory.createContraction());
		return this;
	}
	
	/**
	 * Append minor intonation group
	 * 
	 * @return builder
	 */
	public IPATranscriptBuilder appendMinorIntonationGroup() {
		append(factory.createMinorIntonationGroup());
		return this;
	}
	
	/**
	 * Append major intonation group
	 * 
	 * @return builder
	 */
	public IPATranscriptBuilder appendMajorIntonationGroup() {
		append(factory.createMajorIntonationGroup());
		return this;
	}
	
	/**
	 * Return the {@link IPATranscript} object
	 * 
	 * @return ipa
	 */
	public IPATranscript toIPATranscript() {
		return new IPATranscript(buffer);
	}
	
}
