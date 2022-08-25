/*
 * Copyright (C) 2005-2022 Gregory Hedlund & Yvan Rose
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

package ca.phon.util.alignedTypesDatabase;

import java.io.Serializable;

public final class TierInfo implements Serializable {

	private static final long serialVersionUID = 1812799037424107905L;

	private final String tierName;

	private String tierFont;

	private int order = -1;

	public TierInfo(String tierName) {
		this(tierName, "default");
	}

	public TierInfo(String tierName, String tierFont) {
		this.tierName = tierName;
		this.tierFont = tierFont;
	}

	public String getTierName() {
		return tierName;
	}

	public String getTierFont() {
		return tierFont;
	}

	public void setTierFont(String tierFont) {
		this.tierFont = tierFont;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public TierInfo clone() {
		TierInfo retVal = new TierInfo(tierName);
		retVal.setTierFont(tierFont);
		retVal.setOrder(order);
		return  retVal;
	}

}
