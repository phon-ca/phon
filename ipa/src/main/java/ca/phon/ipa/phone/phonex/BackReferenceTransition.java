package ca.phon.ipa.phone.phonex;

import ca.phon.fsa.FSAState;
import ca.phon.ipa.phone.Phone;

public class BackReferenceTransition extends PhonexTransition {
	
	/**
	 * Group index
	 */
	private int groupIndex = 0;
	
	/**
	 * Length of last group matched when
	 * {@link #follow(FSAState)} returned
	 * <code>true</code>
	 * 
	 */
	public int matchLength = 0;
	
	/**
	 * Constructor
	 * 
	 * @param groupIndex
	 * @param secondaryMatchers
	 */
	public BackReferenceTransition(int groupIndex, PhoneMatcher ... secondaryMatchers) {
		super(null, secondaryMatchers);
		this.groupIndex = groupIndex;
	}
	
	@Override
	public boolean follow(FSAState<Phone> currentState)  {
		boolean retVal = false;
		
		Phone[] groupVal = currentState.getGroup(groupIndex);
		if(groupVal != null && groupVal.length > 0) {
			retVal = true;
			int pIdx = currentState.getTapeIndex();
			for(int i = 0; i < groupVal.length; i++) {
				Phone groupPhone = groupVal[i];
				
				// make sure there is enough input left on the tape
				if(pIdx+1 >= currentState.getTape().length) {
					retVal = false;
					break;
				}
	
				Phone tapePhone = currentState.getTape()[pIdx+i];
				
				retVal &= tapePhone.getText().equals(groupPhone.getText());
				
				// check plug-in matchers
				for(PhoneMatcher pm:getSecondaryMatchers()) {
					retVal &= pm.matches(tapePhone);
				}
				
			}
			if(retVal) {
				matchLength = groupVal.length;
			}
		}
		
		return retVal;
	}
	
	@Override
	public int getMatchLength() {
		return matchLength;
	}
	
	
	@Override
	public String getImage() {
		String retVal = "\\" + groupIndex;
		for(PhoneMatcher pm:getSecondaryMatchers()) {
			retVal += ":" + pm.toString();
		}
		return retVal;
	}
	
	@Override
	public Object clone() {
		BackReferenceTransition retVal = new BackReferenceTransition(groupIndex, getSecondaryMatchers().toArray(new PhoneMatcher[0]));
		retVal.setFirstState(getFirstState());
		retVal.setToState(getToState());
		retVal.getInitGroups().addAll(getInitGroups());
		retVal.getMatcherGroups().addAll(getMatcherGroups());
		return retVal;
	}
}
