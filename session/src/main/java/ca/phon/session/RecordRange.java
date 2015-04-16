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
package ca.phon.session;

import ca.phon.util.Tuple;

public class RecordRange extends Tuple<String, GroupRange> {

	public RecordRange(String tier, GroupRange r) {
		super(tier, r);
	}

	public String getTier() {
		return super.getObj1();
	}

	public void setTier(String tier) {
		super.setObj1(tier);
	}

	public GroupRange getGroupRange() {
		return super.getObj2();
	}

	public void setGroupRange(GroupRange r) {
		super.setObj2(r);
	}
	
	public RecordLocation start() {
		return new RecordLocation(getTier(), getGroupRange().start());
	}
	
	public RecordLocation end() {
		return new RecordLocation(getTier(), getGroupRange().end());
	}
	
}
