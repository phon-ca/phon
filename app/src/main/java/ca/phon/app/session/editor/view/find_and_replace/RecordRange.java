/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.app.session.editor.view.find_and_replace;

import ca.phon.util.Range;
import ca.phon.util.Tuple;

public class RecordRange extends Tuple<String, Range> {

	public RecordRange(String tier, Range r) {
		super(tier, r);
	}

	public String getTier() {
		return super.getObj1();
	}

	public void setTier(String tier) {
		super.setObj1(tier);
	}

	public Range getRange() {
		return super.getObj2();
	}

	public void setRange(Range r) {
		super.setObj2(r);
	}
}
