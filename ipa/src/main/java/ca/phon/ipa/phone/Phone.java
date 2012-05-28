package ca.phon.ipa.phone;

import ca.phon.capability.Capability;
import ca.phon.capability.Extendable;
import ca.phon.ipa.featureset.FeatureSet;
import ca.phon.syllabifier.SyllabificationInfo;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.visitor.Visitable;
import ca.phon.visitor.Visitor;

/**
 * <p>{@link Phone}s represent the atomic unit of IPA transcriptions.
 * {@link Phone}s are constructed using the create methods of
 * {@link PhoneFactory}.</p>
 * 
 * <p>{@link Phone} objects accept are extendable via capabilities.
 * Classes provided to {@link Extendable#putCapability(Class, Object)}
 * must have the {@link Capability} annotation declaring
 * <code>Phone.class</code> as the accepted type.<br/>
 * 
 * E.g.,
 * <pre>
 * &#64;Capability(Phone.class)
 * public class MyNewPhoneCapability {...}
 * </pre>
 * 
 * Common uses for {@link Phone} extensions are annotations such as syllabification
 * information (see {@link SyllabificationInfo}.)</p>
 * 
 * <p>{@link Phone} objects also implement the visitor pattern.  Visitors
 * must implement the {@link PhoneVisitor} or extend {@link PhoneVisitorAdapter}
 * and can be applied using the {@link #accept(PhoneVisitor)} method.</p>
 */
public abstract class Phone extends Extendable implements Visitable<Phone> {
	
	/**
	 * Forced {@link FeatureSet}.  If not <code>null</code>, this
	 * set of features will be returned by {@link #getFeatureSet()}
	 */
	private FeatureSet customFeatureSet = null;
	
	/**
	 * Set the custom features for this {@link Phone}
	 * 
	 * @param featureSet.  Use <code>null</code> to have
	 *  the default features returned
	 */
	public void setFeatureSet(FeatureSet featureSet) {
		this.customFeatureSet = featureSet;
	}
	
	/**
	 * Private method to return the feature set for the
	 * {@link Phone}.  This return value for this method
	 * can be changed by using the {@link #setFeatureSet(FeatureSet)}
	 * method.
	 * 
	 * @return the phones feature set
	 */
	protected abstract FeatureSet _getFeatureSet();
	
	/**
	 * Get the text for this phone
	 * 
	 * @return the phone string
	 */
	public abstract String getText();
	
	/**
	 * Return the feature set for this {@link Phone}.
	 * 
	 * @return the default feature set - derived by the
	 *  implementing type or custom features if defined.
	 */
	public FeatureSet getFeatureSet() {
		FeatureSet retVal = 
				(customFeatureSet != null ? customFeatureSet : _getFeatureSet());
		return retVal;
	}
	
	/**
	 * Direct access to the {@link SyllabificationInfo} capability.
	 * Will return the syllable constituent type for the phone 
	 * (if available.)
	 * 
	 * @return the phone's {@link SyllableConstituentType} or
	 *  {@link SyllableConstituentType#UNKNOWN} if no syllabification
	 *  information was found.
	 */
	public SyllableConstituentType getScType() {
		SyllableConstituentType retVal = SyllableConstituentType.UNKNOWN;
		
		SyllabificationInfo syllInfo = getCapability(SyllabificationInfo.class);
		if(syllInfo != null) {
			retVal = syllInfo.getConstituentType();
		}
		
		return retVal;
	}
	
	@Override
	public void accept(Visitor<Phone> phoneVisitor) {
		phoneVisitor.visit(this);
	}
	
	@Override
	public String toString() {
		return getText();
	}
}
