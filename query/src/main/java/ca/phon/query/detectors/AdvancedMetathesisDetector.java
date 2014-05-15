package ca.phon.query.detectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.ipa.features.FeatureSet;

public class AdvancedMetathesisDetector extends BasicMetathesisDetector {

	private FeatureSet features;
	
	private FeatureSet absentFeatures;
	
	public AdvancedMetathesisDetector() {
		super();
		features = null;
	}
	
	@Override
	public Collection<DetectorResult> detect(PhoneMap pm) {
		final Collection<DetectorResult> potentialResults = super.detect(pm);
		final List<DetectorResult> retVal = new ArrayList<DetectorResult>();
		
		// filter results
		for(DetectorResult r:potentialResults) {
			boolean addResult = true;
			
			if(features != null && features.size() > 0) {
				addResult &= r.getFeatures1().intersects(features) 
					|| r.getFeatures2().intersects(features);
			}
			
			if(absentFeatures != null && absentFeatures.size() > 0) {
				addResult &= !r.getFeatures2().intersects(absentFeatures) 
						&& !r.getFeatures1().intersects(absentFeatures);
			}
			
			if(addResult)
				retVal.add(r);
		}
		
		return retVal;
	}

	public FeatureSet getFeatures() {
		return (features == null ? new FeatureSet() : features);
	}


	public void setFeatures(FeatureSet containedFeatures) {
		this.features = containedFeatures;
	}

	public FeatureSet getAbsentFeatures() {
		return absentFeatures;
	}

	public void setAbsentFeatures(FeatureSet absentFeatures) {
		this.absentFeatures = absentFeatures;
	}
	
}
