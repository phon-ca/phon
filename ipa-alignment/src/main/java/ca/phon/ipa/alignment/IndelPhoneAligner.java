package ca.phon.ipa.alignment;

import java.util.List;

import ca.phon.alignment.AlignmentMap;
import ca.phon.ipa.*;
import ca.phon.syllable.*;

public class IndelPhoneAligner extends IndelAligner<IPAElement> {
	
	private IPATranscript targetRep;
	
	private IPATranscript actualRep;
	
	private List<IPATranscript> targetSylls;
	
	private List<IPATranscript> actualSylls;
	
	private boolean hasStressedSyllables = false;

	public IndelPhoneAligner() {
	}

	@Override
	protected int costSubstitute(IPAElement ele1, IPAElement ele2) {
		int tally = 1;
		
		final SyllableConstituentType t1 = ele1.getScType();
		final SyllableConstituentType t2 = ele2.getScType();
		if(t1 == t2) {
			tally += 2;
		} else {
			tally -= 2;
		}
		
		if(hasStressedSyllables) {
			final SyllableStress s1 = stressForElement(ele1);
			final SyllableStress s2 = stressForElement(ele2);
			if(s1 == s2) {
				++tally;
			} else {
				--tally;
			}
		}
		
		return tally;
	}

	@Override
	protected int costSkip(IPAElement ele) {
		return 0;
	}
	
	public List<IPATranscript> getTargetSyllables() {
		return targetSylls;
	}

	public void setTargetSyllables(List<IPATranscript> targetSylls) {
		this.targetSylls = targetSylls;
	}
	
	public List<IPATranscript> getActualSyllables() {
		return actualSylls;
	}
	
	public void setActualSyllables(List<IPATranscript> actualSylls) {
		this.actualSylls = actualSylls;
	}
	
	public IPATranscript getTargetRep() {
		return targetRep;
	}

	public void setTargetRep(IPATranscript targetRep) {
		this.targetRep = targetRep;
	}

	public IPATranscript getActualRep() {
		return actualRep;
	}

	public void setActualRep(IPATranscript actualRep) {
		this.actualRep = actualRep;
	}

	private IPATranscript syllableContainingElement(IPAElement ele) {
		for(IPATranscript syll:getTargetSyllables()) {
			if(syll.indexOf(ele) >= 0)
				return syll;
		}
		
		for(IPATranscript syll:getActualSyllables()) {
			if(syll.indexOf(ele) >= 0)
				return syll;
		}
		
		return null;
	}
	
	private SyllableStress stressForElement(IPAElement ele) {
		final IPATranscript syll = syllableContainingElement(ele);
		return (syll != null ? syll.getExtension(SyllableStress.class) : null);
	}
	
	public PhoneMap calculatePhoneAlignment(IPATranscript ipaTarget, IPATranscript ipaActual) {
		setTargetRep(ipaTarget);
		setActualRep(ipaActual);
		
		setTargetSyllables(ipaTarget.syllables());
		setActualSyllables(ipaActual.syllables());
		
		for(IPATranscript syll:getTargetSyllables()) {
			final SyllableStress stress = syll.getExtension(SyllableStress.class);
			if(stress == SyllableStress.PrimaryStress || stress == SyllableStress.SecondaryStress) {
				hasStressedSyllables = true;
				break;
			}
		}
		
		final IPATranscript targetPhones = ipaTarget.audiblePhones();
		final IPATranscript actualPhones = ipaActual.audiblePhones();
		
		final IPAElement targetEles[] = new IPAElement[targetPhones.length()];
		for(int i = 0; i < targetPhones.length(); i++) targetEles[i] = targetPhones.elementAt(i);
		
		final IPAElement actualEles[] = new IPAElement[actualPhones.length()];
		for(int i = 0; i < actualPhones.length(); i++) actualEles[i] = actualPhones.elementAt(i);
		
		final AlignmentMap<IPAElement> alignment = calculateAlignment(targetEles, actualEles);
		
		// sort mappings
		Integer[] topAlignment = alignment.getTopAlignment();
		Integer[] bottomAlignment = alignment.getBottomAlignment();
//		Integer temp[][] = new Integer[2][];
//		temp[0] = topAlignment;
//		temp[1] = bottomAlignment;
//		swapIndels(alignment);
		
		final PhoneMap retVal = new PhoneMap(ipaTarget, ipaActual);
		retVal.setTopElements(targetEles);
		retVal.setBottomElements(actualEles);
		retVal.setTopAlignment(topAlignment);
		retVal.setBottomAlignment(bottomAlignment);
		
		return retVal;
	}
	
//	/**
//	 * Swaps cases of adjacent indels where the ordering should be reversed
//	 * 
//	 * @param alignment
//	 */
//	private void swapIndels(Integer[][] alignment) {
//		int lastTop = -2;
//		int lastBottom = -2;
//		
//		for(int i = 0; i < alignment[0].length; i++) {
//			int top = alignment[0][i];
//			int bottom = alignment[1][i];
//			if(i > 0) {
//				// check for need to swap
//				if(lastBottom == -1 && top == -1
//						&& lastTop >=0 && bottom >= 0) {
//					if(bottom < lastTop) {
//						// swap
//						alignment[0][i-1] = top;
//						alignment[1][i-1] = bottom;
//						
//						alignment[0][i] = lastTop;
//						alignment[1][i] = lastBottom;
//					}
//				}
//			}
//			
//			lastTop = top;
//			lastBottom = bottom;
//		}
//	}
		
}
