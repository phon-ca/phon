/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

package ca.phon.script.params;

import java.util.Collection;

import ca.phon.util.Tuple;

/**
 * A group of script parameters.
 */
public class ScriptParamGroup extends Tuple<ScriptParam, ScriptParam[]>{

	public ScriptParamGroup(ScriptParam sep, Collection<ScriptParam> params) {
		this(sep, params.toArray(new ScriptParam[0]));
	}

	public ScriptParamGroup(ScriptParam sep, ScriptParam[] params) {
		super(sep, params);
	}

	public ScriptParam getSeparator() {
		return super.getObj1();
	}

	public ScriptParam[] getParams() {
		return super.getObj2();
	}

	public boolean hasChanged() {
		boolean retVal = false;

		for(ScriptParam sp:getObj2()) {
			retVal |= sp.hasChanged();
		}

		return retVal;
	}
}
