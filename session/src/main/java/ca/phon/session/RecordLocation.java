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
package ca.phon.session;

import ca.phon.util.Tuple;

public class RecordLocation extends Tuple<String, GroupLocation> {

	public RecordLocation(String tier, GroupLocation pos) {
		super(tier, pos);
	}

	public String getTier() {
		return super.getObj1();
	}

	public void setTier(String tier) {
		super.setObj1(tier);
	}

	public GroupLocation getGroupLocation() {
		return super.getObj2();
	}

	public void setGroupLocation(GroupLocation loc) {
		super.setObj2(loc);
	}
}
