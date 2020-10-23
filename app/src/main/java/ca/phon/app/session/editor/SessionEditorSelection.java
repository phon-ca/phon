/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.session.editor;

import ca.phon.util.*;

/**
 * Selection information used for {@link EditorSelectionModel}
 */
public class SessionEditorSelection {
	
	private final int recordIndex;
	
	private final String tierName;
	
	private final int groupIndex;
	
	private final Range groupRange;

	public SessionEditorSelection(int recordIndex, String tierName,
			int groupIndex, Range groupRange) {
		super();
		this.recordIndex = recordIndex;
		this.tierName = tierName;
		this.groupIndex = groupIndex;
		this.groupRange = groupRange;
	}

	public int getRecordIndex() {
		return recordIndex;
	}

	public String getTierName() {
		return tierName;
	}

	public int getGroupIndex() {
		return groupIndex;
	}

	public Range getGroupRange() {
		return groupRange;
	}
	
}
