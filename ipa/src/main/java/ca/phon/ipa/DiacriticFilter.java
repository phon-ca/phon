package ca.phon.ipa;

import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

public class DiacriticFilter extends VisitorAdapter<IPAElement> {

	final IPATranscriptBuilder builder = new IPATranscriptBuilder();
	
	public IPATranscript getIPATranscript() {
		return builder.toIPATranscript();
	}
	
	@Override
	public void fallbackVisit(IPAElement obj) {
		builder.append(obj);
	}
	
	@Visits
	public void visitPhone(Phone phone) {
		builder.append(phone.getBase());
	}
	
	@Visits
	public void visitCompoundPhone(CompoundPhone phone) {
		visit(phone.getFirstPhone());
		visit(phone.getSecondPhone());
		builder.makeCompoundPhone(phone.getLigature());
	}
	
}
