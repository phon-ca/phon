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
package ca.phon.ipa;

import ca.phon.extensions.*;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.syllable.*;
import ca.phon.visitor.*;

import java.beans.*;
import java.util.Set;

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
	
	/*
	 * XXX These methods cause issues if implemented.  Need to track down what code
	 * uses equals in a non-content way
	@Override
	public int hashCode() {
		String hashTxt = getText() + ":" + getScType().getIdChar();
		return hashTxt.hashCode();
	}

	@Override
	public boolean equals(Object ele) {
		if(!(ele instanceof IPAElement)) return false;
		String s1 = getText() + ":" + getScType().getIdChar();
		String s2 = ((IPAElement)ele).getText() + ":" + ((IPAElement)ele).getScType().getIdChar();
		return s1.contentEquals(s2);
	}
	*/
	
	public boolean contentEquals(IPAElement ele) {
		String s1 = getText() + ":" + getScType().getIdChar();
		String s2 = ((IPAElement)ele).getText() + ":" + ((IPAElement)ele).getScType().getIdChar();
		return s1.contentEquals(s2);
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
