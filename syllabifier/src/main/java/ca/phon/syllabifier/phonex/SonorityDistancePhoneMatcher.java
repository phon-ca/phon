package ca.phon.syllabifier.phonex;

import ca.phon.ipa.IPAElement;
import ca.phon.phonex.PhoneMatcher;

/**
 * Phone matcher for sonority distance.
 *
 */
public class SonorityDistancePhoneMatcher implements PhoneMatcher {
	
	/**
	 * Minimum distance
	 */
	private int minDistance;
	
	/**
	 * Allow flat sonority? (default:false)
	 */
	private boolean allowFlat;

	public SonorityDistancePhoneMatcher() {
		this(0, false);
	}
	
	public SonorityDistancePhoneMatcher(int min) {
		this(min, false);
	}
	
	public SonorityDistancePhoneMatcher(int min, boolean allowFlat) {
		this.minDistance = min;
		this.allowFlat = allowFlat;
	}
	
	@Override
	public boolean matches(IPAElement p) {
		boolean retVal = false;
		
		int distance = 0;
		SonorityInfo info = p.getExtension(SonorityInfo.class);
		if(info != null) {
			distance = info.getDistance();
		}
		if(Math.abs(distance) > 0) {
			if(this.minDistance < 0) {
				retVal = distance <= this.minDistance;
			} else {
				retVal = distance >= this.minDistance;
			}
		} else {
			retVal = allowFlat;
		}
		
		return retVal;
	}

	@Override
	public boolean matchesAnything() {
		return false;
	}

}
