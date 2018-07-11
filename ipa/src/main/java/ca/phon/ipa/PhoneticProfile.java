package ca.phon.ipa;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import ca.phon.ipa.features.FeatureSet;

/**
 * Phonetic profile for {@link IPAElement}
 *
 */
public class PhoneticProfile {

	private Map<PhoneDimension, FeatureSet> profile;

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

	public boolean isConsonant() {
		return (ele != null && ele.getFeatureSet().hasFeature("c"));
	}

	public boolean isVowel() {
		return (ele != null && ele.getFeatureSet().hasFeature("v"));
	}

	public boolean isGlide() {
		return (ele != null && ele.getFeatureSet().hasFeature("g"));
	}

	public void clearProfile() {
		put(PhoneDimension.PLACE, new FeatureSet());
		put(PhoneDimension.MANNER, new FeatureSet());
		put(PhoneDimension.VOICING, new FeatureSet());

		put(PhoneDimension.HEIGHT, new FeatureSet());
		put(PhoneDimension.BACKNESS, new FeatureSet());
		put(PhoneDimension.TENSENESS, new FeatureSet());
		put(PhoneDimension.ROUNDING, new FeatureSet());
	}

	private void updateProfile() {
		clearProfile();

		final FeatureSet features = (ele != null ? ele.getFeatureSet() : new FeatureSet());
		if(ele == null)
			return;
		else if(ele.getFeatureSet().hasFeature("Consonant")) {
			profile.put(PhoneDimension.PLACE, FeatureSet.intersect(features, PhoneDimension.PLACE.getFeatures()));
			profile.put(PhoneDimension.MANNER, FeatureSet.intersect(features, PhoneDimension.MANNER.getFeatures()));
			profile.put(PhoneDimension.VOICING, FeatureSet.intersect(features, PhoneDimension.VOICING.getFeatures()));
		} else if(ele.getFeatureSet().hasFeature("Vowel")) {
			profile.put(PhoneDimension.HEIGHT, FeatureSet.intersect(features, PhoneDimension.HEIGHT.getFeatures()));
			profile.put(PhoneDimension.BACKNESS, FeatureSet.intersect(features, PhoneDimension.BACKNESS.getFeatures()));
			profile.put(PhoneDimension.TENSENESS, FeatureSet.intersect(features, PhoneDimension.TENSENESS.getFeatures()));
			profile.put(PhoneDimension.ROUNDING, FeatureSet.intersect(features, PhoneDimension.ROUNDING.getFeatures()));
		}
	}

	public void clear() {
		profile.clear();
	}

	public void put(PhoneDimension dimension, FeatureSet value) {
		profile.put(dimension, value);
	}

	public FeatureSet get(PhoneDimension dimension) {
		return profile.get(dimension);
	}

	public Map<PhoneDimension, FeatureSet> getProfile() {
		return Collections.unmodifiableMap(profile);
	}

	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();

		int idx = 0;
		for(PhoneDimension pd:profile.keySet()) {
			FeatureSet fs = profile.get(pd);
			if(idx++ > 0) buffer.append(", ");
			buffer.append(StringUtils.capitalize(pd.name().toLowerCase())).append("=");

			if(fs != null && fs.size() > 0)
				buffer.append(fs.toString());
			else
				buffer.append("\u2205");
		}

		return buffer.toString();
	}

}
