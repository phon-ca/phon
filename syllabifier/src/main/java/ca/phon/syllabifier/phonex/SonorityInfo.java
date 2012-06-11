package ca.phon.syllabifier.phonex;

import ca.phon.extensions.Extension;
import ca.phon.ipa.phone.Phone;

/**
 * Sonority information used during syllabification.
 *
 */
@Extension(Phone.class)
public class SonorityInfo {
	
	/**
	 * Sonority value, an integer
	 */
	private int sonority = 0;
	
	/**
	 * Sonority distance from previous phone
	 */
	private int distance = 0;
	
	public SonorityInfo() {
		
	}
	
	public SonorityInfo(int sonority, int distance) {
		this.sonority = sonority;
		this.distance = distance;
	}

	public int getSonority() {
		return sonority;
	}

	public void setSonority(int sonority) {
		this.sonority = sonority;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

}
