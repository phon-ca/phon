/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.script.params;

public class SeparatorScriptParam extends ScriptParam {

	private boolean collapsed = false;
	
	public SeparatorScriptParam(String desc) {
		this(desc, false);
	}
	
	public SeparatorScriptParam(String desc, boolean collapsed) {
		super(new String[0], new Object[0]);
		setParamType("separator");
		setParamDesc(desc);
		setCollapsed(collapsed);
	}
	
	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean collapsed) {
		final boolean wasCollapsed = this.collapsed;
		this.collapsed = collapsed;
	}

	@Override
	public String getStringRepresentation() {
		String retVal = "{";

		retVal += "separator, ";
		retVal += "\"" + super.getParamDesc() + "\", ";
		retVal += collapsed;

		retVal += "}";

		return retVal;
	}
}
