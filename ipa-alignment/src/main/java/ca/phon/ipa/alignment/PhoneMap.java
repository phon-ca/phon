/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.util.Set;

import ca.phon.alignment.AlignmentMap;
import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.ipa.AudiblePhoneVisitor;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
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
				sb.append(":").append(topEle.getScType().getIdentifier());
			}
			sb.append(PhonConstants.doubleArrow);
			sb.append( (btmEle != null ? btmEle.getText() : PhonConstants.nullChar) );
			if(includeScType && btmEle != null) {
				sb.append(":").append(btmEle.getScType().getIdentifier());
			}
		}
		
		return sb.toString();
	}
	
}
