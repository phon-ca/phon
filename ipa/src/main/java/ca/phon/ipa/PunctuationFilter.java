package ca.phon.ipa;

import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Phone visitor for filtering punctuation in transcriptions.
 * Flag to ignore or include word boundaries can be set during
 * construction.
 */
public class PunctuationFilter extends VisitorAdapter<IPAElement> {
	
	/**
	 * filtered transcript
	 */
	private final IPATranscriptBuilder builder;
	
	private final boolean ignoreWordBoundaries;
	
	public PunctuationFilter() {
		this(false);
	}
	
	public PunctuationFilter(boolean ignoreWordBoundaries) {
		builder = new IPATranscriptBuilder();
		this.ignoreWordBoundaries = ignoreWordBoundaries;
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
	
	@Visits
	public void visitWordBoundary(WordBoundary wb) {
		if(!ignoreWordBoundaries)
			builder.appendWordBoundary();
	}
	
	@Visits
	public void visitAlignmentMarker(AlignmentMarker marker) {
		builder.append((new IPAElementFactory()).createAlignmentMarker());
	}
	
	public IPATranscript getIPATranscript() {
		return builder.toIPATranscript();
	}
}