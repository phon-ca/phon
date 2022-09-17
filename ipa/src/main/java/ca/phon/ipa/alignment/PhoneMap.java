/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.phon.ipa.alignment;

import ca.phon.alignment.AlignmentMap;
import ca.phon.extensions.*;
import ca.phon.ipa.*;
import ca.phon.phonex.*;
import ca.phon.util.PhonConstants;

import java.util.*;

/**
 *
 */
public class PhoneMap extends AlignmentMap<IPAElement> implements IExtendable {
	/** The target phonetic rep */
	private IPATranscript targetRep;
	/** The actual phonetic rep */
	private IPATranscript actualRep;

	private final ExtensionSupport extSupport = new ExtensionSupport(PhoneMap.class, this);

	public PhoneMap() {
		this(new IPATranscript(), new IPATranscript());
	}

	/**
	 * Constructor
	 */
	public PhoneMap(IPATranscript targetRep, IPATranscript actualRep) {
		super();

		extSupport.initExtensions();

		setTargetRep(targetRep);
		setActualRep(actualRep);
	}

	public IPATranscript getActualRep() {
		return actualRep;
	}

	public void setActualRep(IPATranscript actualRep) {
		this.actualRep = actualRep;

		final AudiblePhoneVisitor visitor = new AudiblePhoneVisitor();
		actualRep.accept(visitor);

		this.bottomElements =
			visitor.getPhones().toArray(new IPAElement[0]);
	}

	public IPATranscript getTargetRep() {
		return targetRep;
	}

	public void setTargetRep(IPATranscript targetRep) {
		this.targetRep = targetRep;

		final AudiblePhoneVisitor visitor = new AudiblePhoneVisitor();
		targetRep.accept(visitor);

		this.topElements =
			visitor.getPhones().toArray(new IPAElement[0]);
	}

	@Override
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}

	@Override
	public String toString() {
		return toString(false);
	}

	public String toString(boolean includeScType) {
		final IPAElement[] topEles = getTopElements();
		final IPAElement[] btmEles = getBottomElements();
		final Integer[] topAlign = getTopAlignment();
		final Integer[] btmAlign = getBottomAlignment();

		if(btmAlign.length != getAlignmentLength() || topAlign.length != getAlignmentLength()) return "";

		final StringBuilder sb = new StringBuilder();
		for(int i = 0; i < getAlignmentLength(); i++) {
			final int topEleIdx = (topAlign[i] == null ? -1 : topAlign[i]);
			final int btmEleIdx = (btmAlign[i] == null ? -1 : btmAlign[i]);

			final IPAElement topEle =
					(topEleIdx >= 0 && topEleIdx < topEles.length ? topEles[topEleIdx] : null);
			final IPAElement btmEle =
					(btmEleIdx >= 0 && btmEleIdx < btmEles.length ? btmEles[btmEleIdx] : null);

			if(i > 0)
				sb.append(',');
			sb.append( (topEle != null ? topEle.getText() : PhonConstants.nullChar) );
			if(includeScType && topEle != null) {
				sb.append(":").append(topEle.getScType().getIdChar());
			}
			sb.append(PhonConstants.doubleArrow);
			sb.append( (btmEle != null ? btmEle.getText() : PhonConstants.nullChar) );
			if(includeScType && btmEle != null) {
				sb.append(":").append(btmEle.getScType().getIdChar());
			}
		}

		return sb.toString();
	}

	/**
	 * Utility method for building a PhoneMap object given a string provided by
	 * the toString method. Original transcriptions are also required.
	 *
	 * @param target
	 * @param actual
	 * @param align the string representation of the alignment
	 *
	 * @throws IllegalArgumentException if the phones found in the align string
	 *  do not match the give target/actual forms
	 */
	public static PhoneMap fromString(IPATranscript target, IPATranscript actual,
			String align) {
		final String phonex = "(.+)\\u2194(.+)";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);

		final String[] alignments = align.split(",");
		int alignLen = alignments.length;

		IPATranscript targetPhones = target.audiblePhones();
		IPATranscript actualPhones = actual.audiblePhones();

		int topPhoneIdx = 0;
		int btmPhoneIdx = 0;
		Integer alignment[][] = new Integer[2][alignLen];
		for(int i = 0; i < alignLen; i++) {
			final IPATranscript alignedPhones =
					(new IPATranscriptBuilder()).append(alignments[i]).toIPATranscript();
			final PhonexMatcher matcher = pattern.matcher(alignedPhones);
			if(matcher.matches()) {
				final IPAElement g1 = new IPATranscript(matcher.group(1)).elementAt(0);
				final IPAElement g2 = new IPATranscript(matcher.group(2)).elementAt(0);

				final IPAElement tele = (topPhoneIdx < targetPhones.length() ? targetPhones.elementAt(topPhoneIdx) : null);
				final IPAElement aele = (btmPhoneIdx < actualPhones.length() ? actualPhones.elementAt(btmPhoneIdx) : null);

				Integer alignCol[] = new Integer[2];
				if(g1.getFeatureSet().hasFeature("null")) {
					alignCol[0] = -1;
				} else if(tele != null && g1.toString().equals(tele.toString())) {
					alignCol[0] = topPhoneIdx++;
				} else {
					// transcripts don't match
					throw new IllegalArgumentException(target + " is not the correct source transcript");
				}

				if(g2.getFeatureSet().hasFeature("null")) {
					alignCol[1] = -1;
				} else if(aele != null && g2.toString().equals(aele.toString())) {
					alignCol[1] = btmPhoneIdx++;
				} else {
					throw new IllegalArgumentException(actual + " is not the correct source transcript");
				}
				alignment[0][i] = alignCol[0];
				alignment[1][i] = alignCol[1];
			} else {
				throw new IllegalArgumentException(align);
			}
		}

		PhoneMap retVal = new PhoneMap(target, actual);
		retVal.setTopAlignment(alignment[0]);
		retVal.setBottomAlignment(alignment[1]);
		return retVal;
	}
	
	/**
	 * Construct alignment given the input string.
	 * This method constructs the IPATranscript references
	 * from the given input.
	 * 
	 * @param align
	 * @return alignment
	 * 
	 * @throws IllegalArgumentException if align is not a valid string
	 */
	public static PhoneMap fromString(String align) {
		final String phonex = "(.+)\\u2194(.+)";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);

		final String[] alignments = align.split(",");
		int alignLen = alignments.length;
		
		final IPATranscriptBuilder tBuilder = new IPATranscriptBuilder();
		final IPATranscriptBuilder aBuilder = new IPATranscriptBuilder();

		int topPhoneIdx = 0;
		int btmPhoneIdx = 0;
		Integer alignment[][] = new Integer[2][alignLen];
		for(int i = 0; i < alignLen; i++) {
			final IPATranscript alignedPhones =
					(new IPATranscriptBuilder()).append(alignments[i]).toIPATranscript();
			final PhonexMatcher matcher = pattern.matcher(alignedPhones);
			if(matcher.matches()) {
				final IPAElement g1 = new IPATranscript(matcher.group(1)).elementAt(0);
				final IPAElement g2 = new IPATranscript(matcher.group(2)).elementAt(0);

				tBuilder.append(g1);
				aBuilder.append(g2);

				Integer alignCol[] = new Integer[2];
				if(g1.getFeatureSet().hasFeature("null")) {
					alignCol[0] = -1;
				} else {
					alignCol[0] = topPhoneIdx++;
				}

				if(g2.getFeatureSet().hasFeature("null")) {
					alignCol[1] = -1;
				} else {
					alignCol[1] = btmPhoneIdx++;
				}
				alignment[0][i] = alignCol[0];
				alignment[1][i] = alignCol[1];
			} else {
				throw new IllegalArgumentException("Invalid syntax: " + align);
			}
		}

		PhoneMap retVal = new PhoneMap(tBuilder.toIPATranscript(), aBuilder.toIPATranscript());
		retVal.setTopAlignment(alignment[0]);
		retVal.setBottomAlignment(alignment[1]);
		return retVal;
	}

	public int getSubAlignmentIndex(IPATranscript ipaT, IPATranscript ipaA) {
		final int ipaTAlignStart =
				(ipaT.length() > 0 ? getTopAlignmentElements().indexOf(ipaT.elementAt(0)) : -1);
		final int ipaAAlignStart =
				(ipaA.length() > 0 ? getBottomAlignmentElements().indexOf(ipaA.elementAt(0)) : ipaTAlignStart);

		final int alignStart = Math.min(ipaTAlignStart, ipaAAlignStart);
		return alignStart;
	}

	public int getSubAlignmentEnd(IPATranscript ipaT, IPATranscript ipaA) {
		final int ipaTAlignEnd =
				(ipaT.length() > 0 ? getTopAlignmentElements().indexOf(ipaT.elementAt(ipaT.length()-1)) : -1);
		final int ipaAAlignEnd =
				(ipaA.length() > 0 ? getBottomAlignmentElements().indexOf(ipaA.elementAt(ipaA.length()-1)) : ipaTAlignEnd);

		final int alignEnd = Math.max(ipaTAlignEnd, ipaAAlignEnd);
		return alignEnd;
	}

	/**
	 * Get the sub-alignment from the given elements.
	 *
	 * @param topElements
	 * @param btmElements
	 *
	 * @return sub-alignment containing the given top/bottom elements
	 */
	public PhoneMap getSubAlignment(IPATranscript ipaT, IPATranscript ipaA) {
		final PhoneMap retVal = new PhoneMap(ipaT, ipaA);
		final IPATranscript filteredT = ipaT.removePunctuation(true);
		final IPATranscript filteredA = ipaA.removePunctuation(true);

		final PhoneMap grpAlignment = this;
		final int alignStart = getSubAlignmentIndex(filteredT, filteredA);
		final int alignEnd = getSubAlignmentEnd(filteredT, filteredA);

		if(alignStart >= 0 && alignEnd >= alignStart) {
			final int alignLen = alignEnd - alignStart + 1;

			final IPATranscriptBuilder target = new IPATranscriptBuilder();
			final IPATranscriptBuilder actual = new IPATranscriptBuilder();
			final Integer topElements[] = new Integer[alignLen];
			final Integer btmElements[] = new Integer[alignLen];

			// copy alignment, but don't keep elements which are not
			// part of our word transcripts
			for(int i = 0; i < alignLen; i++) {
				final List<IPAElement> alignedPair = grpAlignment.getAlignedElements(alignStart+i);
				final IPAElement tEle = alignedPair.get(0);
				final IPAElement aEle = alignedPair.get(1);

				if(tEle != null) target.append(tEle);
				if(aEle != null) actual.append(aEle);

				final Integer tIdx =
						(tEle == null ? AlignmentMap.INDEL_VALUE : target.size()-1);
				final Integer aIdx =
						(aEle == null ? AlignmentMap.INDEL_VALUE : actual.size()-1);

				topElements[i] = tIdx;
				btmElements[i] = aIdx;
			}

			retVal.setTargetRep(target.toIPATranscript());
			retVal.setActualRep(actual.toIPATranscript());
			retVal.setTopAlignment(topElements);
			retVal.setBottomAlignment(btmElements);
		} else {
			retVal.setTopAlignment(new Integer[0]);
			retVal.setBottomAlignment(new Integer[0]);
		}

		return retVal;
	}

}
