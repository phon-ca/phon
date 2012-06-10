package ca.phon.syllable;

import java.lang.ref.WeakReference;

import ca.phon.extensions.Extension;
import ca.phon.extensions.ExtensionProvider;
import ca.phon.extensions.IExtendable;
import ca.phon.ipa.phone.Phone;

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
public class SyllabificationInfo implements ExtensionProvider {
	
	/**
	 * Property name for constituent type changes
	 */
	public final static String PHONE_SCTYPE = "_sctype_";
	
	/**
	 * Property name for stress changes
	 */
	public final static String PHONE_STRESS = "_stress_";
	
	/**
	 * Property name for dipthong member changes
	 */
	public final static String PHONE_DIPHTHONG_MEMBER = "_diphthong_member_";
	
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
	
	/**
	 * weak reference to parent
	 */
	private final WeakReference<Phone> phoneRef;
	
	public SyllabificationInfo(Phone phone) {
		this(phone, SyllableConstituentType.UNKNOWN);
	}
	
	public SyllabificationInfo(Phone phone, SyllableConstituentType scType) {
		phoneRef = new WeakReference<Phone>(phone);
		this.scType = scType;
	}
	
	/**
	 * Get parent {@link Phone}
	 * 
	 * @return parent phone, may be <code>null</code>
	 *  if the {@link WeakRefernce} to the parent is no 
	 *  longer valid.
	 */
	public Phone getPhone() {
		return phoneRef.get();
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
		final SyllableConstituentType oldType = this.scType;
		this.scType = scType;
		getPhone().firePropertyChange(PHONE_SCTYPE, oldType, this.scType);
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
	 * Set the stress of the parent syllable
	 * 
	 * @param stress
	 */
	public void setStress(SyllableStress stress) {
		final SyllableStress oldStress = this.stress;
		this.stress = stress;
		getPhone().firePropertyChange(PHONE_STRESS, oldStress, stress);
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
			final boolean wasDiphthongMember = this.isDipththongMember;
			this.isDipththongMember = isDiphthongMember;
			getPhone().firePropertyChange(PHONE_DIPHTHONG_MEMBER, wasDiphthongMember, isDiphthongMember);
		}
	}

	@Override
	public void installExtension(IExtendable obj) {
		final Phone p = Phone.class.cast(obj);
		p.putExtension(SyllabificationInfo.class, this);
	}
}
