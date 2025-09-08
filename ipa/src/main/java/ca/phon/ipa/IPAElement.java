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
import ca.phon.visitor.*;

import java.util.Set;

/**
 * <p>{@link IPAElement}s represent the atomic unit of IPA transcriptions.
 * {@link IPAElement}s are constructed using the create methods of
 * {@link IPAElementFactory}.</p>
 */
public abstract class IPAElement implements Visitable<IPAElement>, IExtendable {

    /**
	 * Forced {@link FeatureSet}.  If not <code>null</code>, this
	 * set of features will be returned by {@link #featureSet()}
	 */
	private final FeatureSet overrideFeatureSet;

	/**
	 * Syllable information for the phone.
	 */
	private final SyllableInfo syllableInfo;
	
	/**
	 * Extension support
	 */
	private final ExtensionSupport extensionSupport = new ExtensionSupport(IPAElement.class, this);
	
	public IPAElement(FeatureSet overrideFeatureSet, SyllableInfo syllableInfo) {
		super();
		this.overrideFeatureSet = overrideFeatureSet;
		this.syllableInfo = syllableInfo;
		extensionSupport.initExtensions();
	}

	/**
	 * Private method to return the feature set for the
	 * {@link IPAElement}.
	 * 
	 * @return the feature set for the implementing type.
	 */
	protected abstract FeatureSet _getFeatureSet();
	
	/**
	 * Get the text for this phone
	 * 
	 * @return the phone string
	 */
	public abstract String getText();

	/**
	 * Get syllable information for this phone.
	 *
	 * @return the syllable information for the phone
	 */
	public SyllableInfo syllableInfo() {
		return syllableInfo;
	}

	/**
	 * Get override feature set for this phone.
	 *
	 * @return the override feature set or <code>null</code> if
	 *  no override is defined.
	 */
	public FeatureSet overrideFeatureSet() {
		return overrideFeatureSet;
	}
	
	/**
	 * Return the feature set for this {@link IPAElement}.
	 * 
	 * @return the default feature set - derived by the
	 *  implementing type or custom features if defined.
	 */
	public FeatureSet featureSet() {
        return (overrideFeatureSet != null ? overrideFeatureSet : _getFeatureSet());
	}
	
	/**
	 * Get constituent type for this phone.
	 *
	 * @return the syllable constituent type for the phone (if assigned)
	 */
	public SyllableConstituentType constituentType() {
		return syllableInfo != null ? syllableInfo.constituentType() : SyllableConstituentType.UNKNOWN;
	}

	/**
	 * Get syllable stress for this phone.
	 *
	 * @return the syllable stress for the phone
	 */
	public SyllableStress stress() {
		return syllableInfo != null ? syllableInfo.stress() : SyllableStress.NoStress;
	}

    public boolean segregated() {
        return syllableInfo != null && syllableInfo.segregated();
    }

    public int sonorityDistance() {
        return syllableInfo != null ? syllableInfo.sonorityDistance() : 0;
    }

    public int sonority() {
        return syllableInfo != null ? syllableInfo.sonority() : 0;
    }

    public ToneNumber tone() {
        return syllableInfo != null ? syllableInfo.tone() : null;
    }

    public boolean isDiphthong() {
        return syllableInfo != null && syllableInfo.isDiphthong();
    }

    public int syllableIndex() {
        return syllableInfo != null ? syllableInfo.syllableIndex() : -1;
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
