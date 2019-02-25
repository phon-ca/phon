/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.session;

import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;

public abstract class AlignedSyllable implements IExtendable {
	
	private final ExtensionSupport extSupport = new ExtensionSupport(AlignedSyllable.class, this);
	
	public AlignedSyllable() {
		super();
		
		extSupport.initExtensions();
	}
	
	public abstract Group getGroup();
	
	public abstract Word getWord();
	
	public abstract int getGroupIndex();

	public abstract int getWordIndex();
	
	public abstract int getSyllableIndex();
	
	public abstract IPATranscript getIPATarget();
	
	public abstract IPATranscript getIPAActual();
	
	public abstract int getIPATargetLocation();
	
	public abstract int getIPAActualLocation();

	public abstract PhoneMap getPhoneAlignment();
	
	public abstract int getPhoneAlignmentLocation();
	
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}
	
}