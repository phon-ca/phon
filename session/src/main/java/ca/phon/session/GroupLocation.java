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

public class GroupLocation extends Tuple<Integer, Integer> {
	
	public GroupLocation(Integer groupIndex, Integer charIndex) {
		super(groupIndex, charIndex);
	}
	
	public Integer getGroupIndex() {
		return super.getObj1();
	}
	
	public Integer getCharIndex() {
		return super.getObj2();
	}
	
	public void setGroupIndex(Integer groupIndex) {
		super.setObj1(groupIndex);
	}
	
	public void setCharIndex(Integer charIndex) {
		super.setObj2(charIndex);
	}

}
