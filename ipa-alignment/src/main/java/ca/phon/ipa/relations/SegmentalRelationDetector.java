package ca.phon.ipa.relations;

import java.util.Optional;

import ca.phon.ipa.alignment.PhoneMap;

public interface SegmentalRelationDetector {
	
	/**
	 * Returns an optional segmental relation.  isPresent()
	 * will return <code>false</code> when relation was
	 * not detected.
	 * 
	 * 
	 * @param pm
	 * @param p1
	 * @param p2
	 * @return
	 */
	public Optional<SegmentalRelation> detect(PhoneMap pm, int p1, int p2);
	
}
