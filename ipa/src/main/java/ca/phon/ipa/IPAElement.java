package ca.phon.ipa;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.ipa.elements.IPAElementFactory;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.syllable.SyllabificationInfo;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.visitor.Visitable;
import ca.phon.visitor.Visitor;

/**
 * <p>{@link IPAElement}s represent the atomic unit of IPA transcriptions.
 * {@link IPAElement}s are constructed using the create methods of
 * {@link IPAElementFactory}.</p>
 * 
 * <p>{@link IPAElement} objects are extendable via capabilities.
 * Classes provided to {@link ExtensionSupport#putExtension(Class, Object)}
 * must have the {@link Extension} annotation declaring
 * <code>Phone.class</code> as the accepted type.<br/>
 * 
 * E.g.,
 * <pre>
 * &#64;Extension(IPAElement.class)
 * public class MyNewPhoneExtension {...}
 * </pre>
 * 
 * Common uses for {@link IPAElement} extensions are annotations such as syllabification
 * information (see {@link SyllabificationInfo}.)</p>
 * 
 * <p>{@link IPAElement} objects also implement the visitor pattern.  Visitors
 * must implement the {@link PhoneVisitor} or extend {@link PhoneVisitorAdapter}
 * and can be applied using the {@link #accept(PhoneVisitor)} method.</p>
 */
public abstract class IPAElement implements Visitable<IPAElement>, IExtendable {
	
	/**
	 * Property name for changes on phone text
	 */
	public final static String PHONE_TEXT = "_text_";
	
	/**
	 * Forced {@link FeatureSet}.  If not <code>null</code>, this
	 * set of features will be returned by {@link #getFeatureSet()}
	 */
	private FeatureSet customFeatureSet = null;
	
	/**
	 * Extension support
	 */
	private final ExtensionSupport extensionSupport = new ExtensionSupport(IPAElement.class, this);
	
	/**
	 * Property change support
	 */
	private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
	
	public IPAElement() {
		super();
		extensionSupport.initExtensions();
	}
	
	/**
	 * Set the custom features for this {@link IPAElement}
	 * 
	 * @param featureSet.  Use <code>null</code> to have
	 *  the default features returned
	 */
	public void setFeatureSet(FeatureSet featureSet) {
		this.customFeatureSet = featureSet;
	}
	
	/**
	 * Private method to return the feature set for the
	 * {@link IPAElement}.  This return value for this method
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
	 * Return the feature set for this {@link IPAElement}.
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
	 * Direct access to {@link SyllabificationInfo#getConstituentType()}.
	 * Will return the syllable constituent type for the phone 
	 * (if available.)
	 * 
	 * @return the phone's {@link SyllableConstituentType} or
	 *  {@link SyllableConstituentType#UNKNOWN} if no syllabification
	 *  information was found.
	 */
	public SyllableConstituentType getScType() {
		SyllableConstituentType retVal = SyllableConstituentType.UNKNOWN;
		
		SyllabificationInfo syllInfo = getExtension(SyllabificationInfo.class);
		if(syllInfo != null) {
			retVal = syllInfo.getConstituentType();
		}
		
		return retVal;
	}
	
	/**
	 * Direct access to {@link IPAElement}s {@link SyllabificationInfo#setConstituentType(SyllableConstituentType)}.
	 * 
	 * @param scType the constituent type for the phon
	 */
	public void setScType(SyllableConstituentType scType) {
		SyllabificationInfo syllInfo = getExtension(SyllabificationInfo.class);
		if(syllInfo == null) {
			syllInfo = new SyllabificationInfo(this);
			putExtension(SyllabificationInfo.class, syllInfo);
		}
		syllInfo.setConstituentType(scType);
	}
	
	//
	// Props
	//
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(propertyName, listener);
	}
	
	public void fireIndexedPropertyChange(String propertyName, int index,
			boolean oldValue, boolean newValue) {
		propSupport.fireIndexedPropertyChange(propertyName, index, oldValue,
				newValue);
	}

	public void fireIndexedPropertyChange(String propertyName, int index,
			int oldValue, int newValue) {
		propSupport.fireIndexedPropertyChange(propertyName, index, oldValue,
				newValue);
	}

	public void fireIndexedPropertyChange(String propertyName, int index,
			Object oldValue, Object newValue) {
		propSupport.fireIndexedPropertyChange(propertyName, index, oldValue,
				newValue);
	}

	public void firePropertyChange(PropertyChangeEvent event) {
		propSupport.firePropertyChange(event);
	}

	public void firePropertyChange(String propertyName, boolean oldValue,
			boolean newValue) {
		propSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, int oldValue,
			int newValue) {
		propSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		propSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
		return propSupport.getPropertyChangeListeners();
	}

	public PropertyChangeListener[] getPropertyChangeListeners(
			String propertyName) {
		return propSupport.getPropertyChangeListeners(propertyName);
	}

	public boolean hasListeners(String propertyName) {
		return propSupport.hasListeners(propertyName);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propSupport.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propSupport.removePropertyChangeListener(propertyName, listener);
	}

	//
	// Extensions
	//
	private ExtensionSupport getExtensionSupport() {
		return extensionSupport;
	}
	
	@Override
	public void accept(Visitor<IPAElement> phoneVisitor) {
		phoneVisitor.visit(this);
	}
	
	@Override
	public Set<Class<?>> getExtensions() {
		return getExtensionSupport().getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return getExtensionSupport().getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return getExtensionSupport().putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return getExtensionSupport().removeExtension(cap);
	}

	@Override
	public String toString() {
		return getText();
	}
}
