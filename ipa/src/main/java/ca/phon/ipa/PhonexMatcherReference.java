/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
