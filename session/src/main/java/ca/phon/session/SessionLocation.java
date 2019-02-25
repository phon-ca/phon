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

/**
 * Helper classes for holding current and start locations
 */
public class SessionLocation extends Tuple<Integer, RecordLocation> {

	public SessionLocation(Integer recIdx, RecordLocation recLoc) {
		super(recIdx, recLoc);
	}

	public Integer getRecordIndex() {
		return super.getObj1();
	}

	public void setRecordIndex(Integer idx) {
		super.setObj1(idx);
	}

	public RecordLocation getRecordLocation() {
		return super.getObj2();
	}

	public void setRecordLocation(RecordLocation recLoc) {
		super.setObj2(recLoc);
	}
}