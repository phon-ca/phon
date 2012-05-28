package ca.phon.ipa.phone.phonex;

import java.util.ArrayList;
import java.util.List;

import ca.phon.ipa.phone.BasicPhone;
import ca.phon.ipa.phone.CompoundPhone;
import ca.phon.ipa.phone.Pause;
import ca.phon.ipa.phone.Phone;
import ca.phon.ipa.phone.StressMarker;
import ca.phon.ipa.phone.SyllableBoundary;
import ca.phon.ipa.phone.WordBoundary;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Filter a iterable list of phones based on phonex
 * flags.
 */
public class PhonexFlagVisitor extends VisitorAdapter<Phone> {
	
	/**
	 * Return value
	 */
	private final List<Phone> filteredList = 
			new ArrayList<Phone>();
	
	/**
	 * phonex flags
	 */
	

	@Override
	public void fallbackVisit(Phone obj) {
		
	}
	
	@Visits
	public void basicPhone(BasicPhone phone) {
		
	}
	
	@Visits
	public void compoundPhone(CompoundPhone phone) {
		
	}
	
	@Visits
	public void pause(Pause phone) {
		
	}
	
	@Visits
	public void stress(StressMarker phone) {
		
	}

	@Visits
	public void syllableBoundary(SyllableBoundary phone) {
		
	}
	
	@Visits
	public void wordBoundary(WordBoundary phone) {
		
	}
}
