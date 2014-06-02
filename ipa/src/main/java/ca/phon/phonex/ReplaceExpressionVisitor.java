package ca.phon.phonex;

import java.util.logging.Logger;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipa.PhonexMatcherReference;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

public class ReplaceExpressionVisitor extends VisitorAdapter<IPAElement> {
	
	private static final Logger LOGGER = Logger
			.getLogger(ReplaceExpressionVisitor.class.getName());

	private final PhonexMatcher matcher;
	
	private final IPATranscriptBuilder builder;
	
	public ReplaceExpressionVisitor(PhonexMatcher matcher) {
		super();
		this.matcher = matcher;
		this.builder = new IPATranscriptBuilder();
	}
	
	public IPATranscript getTranscript() {
		return builder.toIPATranscript();
	}

	@Override
	public void fallbackVisit(IPAElement obj) {
		builder.append(obj);
	}
	
	@Visits
	public void visitPhonexMatcherReference(PhonexMatcherReference pmr) {
		int groupIndex = pmr.getGroupIndex();
		if(groupIndex < 0) {
			final String groupName = pmr.getGroupName();
			if(groupName == null) {
				LOGGER.severe("Unknown phonex matcher reference " + pmr.getText());
				return;
			}
			groupIndex = matcher.pattern().groupIndex(groupName);
		}
		builder.append(matcher.group(groupIndex));
	}
	
}
