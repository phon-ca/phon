package ca.phon.ipa;

import java.util.ArrayList;
import java.util.List;

import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Visitor for filtering a list of phones into a list of 
 * audible phones.
 * 
 */
public class AudiblePhoneVisitor extends VisitorAdapter<IPAElement> {

	private final List<IPAElement> phones = new ArrayList<IPAElement>();
	
	@Override
	public void fallbackVisit(IPAElement obj) {	
	}

	@Visits
	public void visitPhone(Phone p) {
		phones.add(p);
	}
	
	@Visits
	public void visitCompoundPhone(CompoundPhone cp) {
		phones.add(cp);
	}
	
	public List<IPAElement> getPhones() {
		return this.phones;
	}
	
}
