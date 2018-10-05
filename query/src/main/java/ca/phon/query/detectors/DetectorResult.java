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
package ca.phon.query.detectors;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.ipa.PhoneDimension;
import ca.phon.ipa.alignment.PhoneMap;

/**
 * A result object for two positions of a {@link PhoneMap}
 * 
 */
public class DetectorResult implements IExtendable {
	
	private final PhoneMap phoneMap;
	protected int pos1;
	protected int pos2;
	
	private Map<PhoneDimension, String> dimensions = new LinkedHashMap<>();
	
	private final ExtensionSupport extSupport = new ExtensionSupport(DetectorResult.class, this);
	
	/**
	 * Default constructor.
	 */
	public DetectorResult(PhoneMap phoneMap) {
		this.pos1 = -1;
		this.pos2 = -1;
		this.phoneMap = phoneMap;
		
		extSupport.initExtensions();
	}
	
    /**
     * Get the first position of the result.
     * @return  the first position
     */
    public PhoneMap getPhoneMap() { return this.phoneMap; }
	
	/**
     * Set the first position of the result.
     * @param p  the position
     */
    public void setFirstPosition(int p) { this.pos1 = p; }
	
    /**
     * Get the first position of the result.
     * @return  the first position
     */
    public int getFirstPosition() { return this.pos1; }
    
    /**
     * Set the second position of the result.
     * @param p  the position
     */
    public void setSecondPosition(int p) { this.pos2 = p; }

    /**
     * Get the second position of the result.
     * @return  the second position
     */
    public int getSecondPosition() { return this.pos2; }

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
