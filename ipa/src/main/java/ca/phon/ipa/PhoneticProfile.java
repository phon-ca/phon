package ca.phon.ipa;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import ca.phon.ipa.features.FeatureSet;

/**
 * Phonetic profile for and {@link IPAElement}
 *
 */
public class PhoneticProfile {
	
	private Map<PhoneDimension, Integer> profile;
	
	private IPAElement ele;
	
	public PhoneticProfile() {
		super();
		
		this.ele = new IPAElement() {
			
			@Override
			public String getText() {
				return "\u2205";
			}
			
			@Override
			protected FeatureSet _getFeatureSet() {
				return new FeatureSet();
			}
			
		};
		profile = new LinkedHashMap<>();
	}
	
	public PhoneticProfile(IPAElement ele) {
		this();
		
		setElement(ele);
	}
	
	public Set<PhoneDimension> getDimensions() {
		return Collections.unmodifiableSet(profile.keySet());
	}
	
	public void setElement(IPAElement ele) {
		this.ele = ele;
		updateProfile();
	}
	
	public IPAElement getElement() {
		return this.ele;
	}
	
	private void updateProfile() {
		profile.clear();
		
		if(ele == null) 
			return;
		else if(ele.getFeatureSet().hasFeature("Consonant")) {
			int placeIdx = PhoneDimension.PLACE.getCategoryIndex(ele);
			profile.put(PhoneDimension.PLACE, placeIdx);

			int mannerIdx = PhoneDimension.MANNER.getCategoryIndex(ele);
			profile.put(PhoneDimension.MANNER, mannerIdx);

			int voicingIdx = PhoneDimension.VOICING.getCategoryIndex(ele);
			profile.put(PhoneDimension.VOICING, voicingIdx);
		} else if(ele.getFeatureSet().hasFeature("Vowel")) {
			int heightIdx = PhoneDimension.HEIGHT.getCategoryIndex(ele);
			profile.put(PhoneDimension.HEIGHT, heightIdx);
			
			int backnessIdx = PhoneDimension.BACKNESS.getCategoryIndex(ele);
			profile.put(PhoneDimension.BACKNESS, backnessIdx);
			
			int tensenessIdx = PhoneDimension.TENSENESS.getCategoryIndex(ele);
			profile.put(PhoneDimension.TENSENESS, tensenessIdx);
			
			int roundingIdx = PhoneDimension.ROUNDING.getCategoryIndex(ele);
			profile.put(PhoneDimension.ROUNDING, roundingIdx);
		}
	}
	
	public void clear() {
		profile.clear();
	}
	
	public void put(PhoneDimension dimension, Integer value) {
		profile.put(dimension, value);
	}
	
	public Map<PhoneDimension, Integer> getProfile() {
		return Collections.unmodifiableMap(profile);
	}

	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		
		int idx = 0;
		for(PhoneDimension pd:profile.keySet()) {
			if(idx++ > 0) buffer.append(", ");
			buffer.append(StringUtils.capitalize(pd.name().toLowerCase())).append("=").append(pd.getCategories()[profile.get(pd)]);
		}
		
		return buffer.toString();
	}
	
}
