/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.phon.ipa.alignment;

import java.util.List;
import java.util.Set;

import ca.phon.alignment.AlignmentMap;
import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.ipa.AudiblePhoneVisitor;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;
import ca.phon.util.PhonConstants;

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

		IPATranscript targetPhones = target.stripDiacritics().removePunctuation();
		IPATranscript actualPhones = actual.stripDiacritics().removePunctuation();

		int topPhoneIdx = 0;
		int btmPhoneIdx = 0;
		Integer alignment[][] = new Integer[2][alignLen];
		for(int i = 0; i < alignLen; i++) {
			final IPATranscript alignedPhones =
					(new IPATranscriptBuilder()).append(alignments[i]).toIPATranscript();
			final PhonexMatcher matcher = pattern.matcher(alignedPhones);
			if(matcher.matches()) {
				final IPAElement g1 = new IPATranscript(matcher.group(1)).stripDiacritics().removePunctuation().elementAt(0);
				final IPAElement g2 = new IPATranscript(matcher.group(2)).stripDiacritics().removePunctuation().elementAt(0);

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
		final int ipaTAlignStart =
				(filteredT.length() > 0 ? grpAlignment.getTopAlignmentElements().indexOf(filteredT.elementAt(0)) : -1);
		final int ipaAAlignStart =
				(filteredA.length() > 0 ? grpAlignment.getBottomAlignmentElements().indexOf(filteredA.elementAt(0)) : -1);
		final int alignStart = Math.min(ipaTAlignStart, ipaAAlignStart);

		final int ipaTAlignEnd =
				(filteredT.length() > 0 ? grpAlignment.getTopAlignmentElements().indexOf(filteredT.elementAt(filteredT.length()-1)) : -1);
		final int ipaAAlignEnd =
				(filteredA.length() > 0 ? grpAlignment.getBottomAlignmentElements().indexOf(filteredA.elementAt(filteredA.length()-1)) : -1);
		final int alignEnd = Math.max(ipaTAlignEnd, ipaAAlignEnd);

		if(alignStart >= 0 && alignEnd >= alignStart) {
			final int alignLen = alignEnd - alignStart + 1;

			final Integer topElements[] = new Integer[alignLen];
			final Integer btmElements[] = new Integer[alignLen];

			// copy alignment, but don't keep elements which are not
			// part of our word transcripts
			for(int i = 0; i < alignLen; i++) {
				final List<IPAElement> alignedPair = grpAlignment.getAlignedElements(alignStart+i);
				final IPAElement tEle = alignedPair.get(0);
				final IPAElement aEle = alignedPair.get(1);

				final Integer tIdx =
						(tEle == null ? AlignmentMap.INDEL_VALUE : filteredT.indexOf(tEle));
				final Integer aIdx =
						(aEle == null ? AlignmentMap.INDEL_VALUE : filteredA.indexOf(aEle));

				topElements[i] = tIdx;
				btmElements[i] = aIdx;
			}

			retVal.setTopAlignment(topElements);
			retVal.setBottomAlignment(btmElements);
		} else {
			retVal.setTopAlignment(new Integer[0]);
			retVal.setBottomAlignment(new Integer[0]);
		}

		return retVal;
	}

}
