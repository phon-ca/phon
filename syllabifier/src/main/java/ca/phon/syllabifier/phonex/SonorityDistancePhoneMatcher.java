package ca.phon.syllabifier.phonex;

import ca.phon.ipa.phone.Phone;
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
	public boolean matches(Phone p) {
		boolean retVal = false;
		
		SonorityInfo info = p.getExtension(SonorityInfo.class);
		if(info != null) {
			final int distance = info.getDistance();
			if(distance >= this.minDistance
					|| (distance == 0 && allowFlat))
				retVal = true;
		}
		
		return retVal;
	}

	@Override
	public boolean matchesAnything() {
		return false;
	}

}
