/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.session.editor;

import ca.phon.util.Range;

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
