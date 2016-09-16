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

import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.ipa.features.FeatureSet;

public abstract class DetectorResult {
	public abstract DetectorResultType getType();
	
	private final PhoneMap phoneMap;
	protected int pos1;
	protected int pos2;
	protected FeatureSet features1;
	protected FeatureSet features2;
	
	/**
	 * Default constructor.
	 */
	public DetectorResult(PhoneMap phoneMap) {
		this.pos1 = -1;
		this.pos2 = -1;
		this.features1 = new FeatureSet();
		this.features2 = new FeatureSet();
		this.phoneMap = phoneMap;
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
    
    /**
     * Get one group of feature(s) involved in the result.
     * @return  the features as a {@link java.util.Collection<String>}
     * @see     #getFeatures2
     */
    public FeatureSet getFeatures1() { return this.features1; }
    
    /**
     * Set one group of feature(s) involved in the result.
     * @return  the features as a {@link java.util.Collection<String>}
     * @see     #setFeatures2
     */
    public void setFeatures1(FeatureSet features) { this.features1 = features; }

    /**
     * Get one group of feature(s) involved in the result.
     * @return  the features as a {@link java.util.Collection<String>}
     * @see     #getFeatures1
     */
    public FeatureSet getFeatures2() { return this.features2; }
    
    /**
     * Set one group of feature(s) involved in the result.
     * @return  the features as a {@link java.util.Collection<String>}
     * @see     #setFeatures1
     */
    public void setFeatures2(FeatureSet features) { this.features2 = features; }
    
    
    
}
