package ca.phon.ipa.alignment;

import ca.phon.alignment.AlignmentMap;
import ca.phon.ipa.*;
import ca.phon.syllable.SyllableConstituentType;

public class IndelPhoneAligner extends IndelAligner<IPAElement> {

	public IndelPhoneAligner() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected int costSubstitute(IPAElement ele1, IPAElement ele2) {
		int tally = 1;
		
//		if(ele1.getFeatureSet().hasFeature("Consonant")) {
//			if(!ele2.getFeatureSet().hasFeature("Consonant")) {
//				--tally;
//			} else {
//				++tally;
//			}
//		}
		
		final SyllableConstituentType t1 = ele1.getScType();
		final SyllableConstituentType t2 = ele2.getScType();
		if(t1 == t2) {
			tally += 2;
		} else {
			tally -= 2;
		}
		
		return tally;
	}

	@Override
	protected int costSkip(IPAElement ele) {
		return 0;
	}

	public PhoneMap calculatePhoneAlignment(IPATranscript ipaTarget, IPATranscript ipaActual) {
		final IPATranscript targetPhones = ipaTarget.audiblePhones();
		final IPATranscript actualPhones = ipaActual.audiblePhones();
		
		final IPAElement targetEles[] = new IPAElement[targetPhones.length()];
		for(int i = 0; i < targetPhones.length(); i++) targetEles[i] = targetPhones.elementAt(i);
		
		final IPAElement actualEles[] = new IPAElement[actualPhones.length()];
		for(int i = 0; i < actualPhones.length(); i++) actualEles[i] = actualPhones.elementAt(i);
		
		final AlignmentMap<IPAElement> alignment = calculateAlignment(targetEles, actualEles);
		final PhoneMap retVal = new PhoneMap(ipaTarget, ipaActual);
		retVal.setTopElements(targetEles);
		retVal.setBottomElements(actualEles);
		retVal.setTopAlignment(alignment.getTopAlignment());
		retVal.setBottomAlignment(alignment.getBottomAlignment());
		
		return retVal;
	}
	
}
