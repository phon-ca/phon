package ca.phon.ipa;

import ca.phon.ipa.features.FeatureSet;

/**
 * A special type of IPAElement which represents a
 * reference to a phonex group.  This is used during
 * phonex replacement only.
 *
 */
public class PhonexMatcherReference extends IPAElement {
	
	private Integer groupIndex;
	
	private String groupName;
	
	public PhonexMatcherReference(Integer groupIndex) {
		this.groupIndex = groupIndex;
	}
	
	public PhonexMatcherReference(String groupName) {
		this.groupName = groupName;
	}
	
	public int getGroupIndex() {
		return (groupIndex == null ? -1 : groupIndex);
	}
	
	public String getGroupName() {
		return groupName;
	}

	@Override
	protected FeatureSet _getFeatureSet() {
		return new FeatureSet();
	}

	@Override
	public String getText() {
		return "$" + 
				(groupName != null ? "{" + groupName + "}" : groupIndex);
	}

}
