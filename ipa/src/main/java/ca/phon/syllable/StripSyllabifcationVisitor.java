package ca.phon.syllable;

import ca.phon.ipa.CompoundPhone;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.Phone;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

public class StripSyllabifcationVisitor extends VisitorAdapter<IPAElement> {

	@Override
	public void fallbackVisit(IPAElement obj) {
	}
	
	@Visits
	public void visitPhone(Phone p) {
		p.setScType(SyllableConstituentType.UNKNOWN);
	}
	
	@Visits
	public void visitCompoundPhone(CompoundPhone cp) {
		cp.setScType(SyllableConstituentType.UNKNOWN);
	}

}
