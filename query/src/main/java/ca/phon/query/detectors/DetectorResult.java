/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
