package ca.phon.syllable;

import ca.phon.capability.Capability;
import ca.phon.capability.Extension;
import ca.phon.capability.IExtendable;
import ca.phon.capability.IExtension;
import ca.phon.ipa.phone.IntonationGroup;
import ca.phon.ipa.phone.Pause;
import ca.phon.ipa.phone.Phone;
import ca.phon.ipa.phone.StressMarker;
import ca.phon.ipa.phone.SyllableBoundary;
import ca.phon.ipa.phone.WordBoundary;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Adds syllabification information to Phones.
 * 
 * Syllabification information includes:
 *  
 * <ul>
 *   <li># of syllables in phrase</li>
 *   <li>syllable index of phone</li>
 *   <li>syllable stress</li>
 *   <li>constituent type</li>
 * </ul>
 */
@Extension(Phone.class)
@Capability(Phone.class)
public class SyllabificationInfo implements IExtension {
	
	/**
	 * The constituent type
	 */
	private SyllableConstituentType scType = SyllableConstituentType.UNKNOWN;
	
	/**
	 * Stress
	 */
	private SyllableStress stress = SyllableStress.NoStress;
	
	/**
	 * Is diphthong member?
	 * If <code>true</code>adjacent nuclei will be
	 * part of the same syllable.
	 */
	private boolean isDipththongMember = false;
	
	public SyllabificationInfo() {
		
	}
	
	public SyllabificationInfo(SyllableConstituentType scType) {
		this.scType = scType;
	}
	
	/**
	 * Return the syllable constituent type.
	 * 
	 * @return the syllable constituent type for
	 *  the phone
	 */
	public SyllableConstituentType getConstituentType() {
		return scType;
	}
	
	/**
	 * Set constituen type
	 * 
	 * @param scType
	 */
	public void setConstituentType(SyllableConstituentType scType) {
		this.scType = scType;
	}
	
	/**
	 * Stress of the parent syllable.
	 * 
	 * @return the stress type of the parent syllable
	 */
	public SyllableStress getStress() {
		return stress;
	}
	
	/**
	 * Is this phone part of a diphthong?
	 * 
	 * @return <code>true</code> if the constituent type of the
	 *  {@link Phone} is {@link SyllableConstituentType#NUCLEUS} and
	 *  the {@link Phone} is part of a diphthong, <code>false</code>
	 *  otherwise
	 */
	public boolean isDiphthongMember() {
		return this.isDipththongMember;
	}
	
	/**
	 * Set this {@link Phone} as part of a diphthong member.
	 * Only valid if {@link #getConstituentType()} is
	 * {@link SyllableConstituentType#NUCLEUS}.
	 * 
	 * @param isDiphthongMember
	 */
	public void setDiphthongMember(boolean isDiphthongMember) {
		if(getConstituentType() == SyllableConstituentType.NUCLEUS) {
			this.isDipththongMember = isDiphthongMember;
		}
	}

	@Override
	public void installExtension(IExtendable obj) {
		Phone p = Phone.class.cast(obj);
		p.putCapability(SyllabificationInfo.class, this);
	}
}
