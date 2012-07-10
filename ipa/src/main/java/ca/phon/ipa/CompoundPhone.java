package ca.phon.ipa;

import ca.phon.ipa.features.FeatureSet;
import ca.phon.ipa.parser.IPATokenType;
import ca.phon.ipa.parser.IPATokens;

/**
 * A compound Phone consists of two phones connected
 * by a ligature.
 * 
 */
public final class CompoundPhone extends IPAElement {
	
	/**
	 * First phone
	 */
	private Phone firstPhone;
	
	/**
	 * Second phone
	 */
	private Phone secondPhone;
	
	/**
	 * Ligature
	 */
	private Character ligature;
	
	
	public CompoundPhone() {
		
	}
	
	/**
	 * Constructor
	 * 
	 * @param firstPhone
	 * @param secondPhone
	 * @param ligature
	 */
	CompoundPhone(Phone firstPhone, Phone secondPhone, Character ligature) {
		super();
		setFirstPhone(firstPhone);
		setSecondPhone(secondPhone);
		setLigature(ligature);
	}
	
	/**
	 * Get the first phone in this compound 
	 * 
	 * @return the first phone
	 */
	public Phone getFirstPhone() {
		return this.firstPhone;
	}
	
	/**
	 * Set the first phone for this compound
	 * 
	 * @param phone
	 */
	public void setFirstPhone(Phone phone) {
		this.firstPhone = phone;
	}
	
	/**
	 * Get second phone
	 * 
	 * @return the second phone
	 */
	public Phone getSecondPhone() {
		return this.secondPhone;
	}

	/**
	 * Set second phone
	 * 
	 * @param phone
	 */
	public void setSecondPhone(Phone phone) {
		this.secondPhone = phone;
	}
	
	/**
	 * Get the ligature used
	 * 
	 * @return the ligature glyph
	 */
	public Character getLigature() {
		return this.ligature;
	}
	
	/**
	 * Set the ligature 
	 * 
	 * @param ligature
	 */
	public void setLigature(Character ligature) {
		IPATokenType tt = 
				IPATokens.getSharedInstance().getTokenType(ligature);
		if(tt == null && tt != IPATokenType.LIGATURE) {
			throw new IllegalArgumentException("ligature must have the token type LIGATURE");
		}
		this.ligature = ligature;
	}
	
	@Override
	protected FeatureSet _getFeatureSet() {
		FeatureSet retVal = new FeatureSet();
		retVal.union(firstPhone.getFeatureSet());
		retVal.union(secondPhone.getFeatureSet());
		return retVal;
	}

	@Override
	public String getText() {
		return firstPhone.getText() + ligature + secondPhone.getText();
	}

}
