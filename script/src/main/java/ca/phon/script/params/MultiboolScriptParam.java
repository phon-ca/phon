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
package ca.phon.script.params;

import java.util.Iterator;

public class MultiboolScriptParam extends ScriptParam {
	
	/** Descriptions */
	private String[] descs;
	
	/** Number of columns */
	private int numCols = 2;
	
	public MultiboolScriptParam(String[] ids, Boolean[] defaults, String[] descs, String desc, int numCols) {
		super(ids, defaults);
		
		setParamType("multibool");
		setParamDesc(desc);
		
		this.descs = descs;
		this.numCols = numCols;
	}
	
	@Override
	public String getStringRepresentation() {
		String retVal = "{";

		Iterator<String> it = super.getParamIds().iterator();
		String ids = null;
		String defs = null;
		String lbls = null;
		int idx = 0;
		while(it.hasNext()) {
			String id = it.next();
			if(ids == null)
				ids = id;
			else
				ids += "|" + id;

			if(defs == null)
				defs = super.getDefaultValue(id).toString();
			else
				defs += "|" + super.getDefaultValue(id).toString();

			if(idx == 0)
				lbls = "\"" + descs[idx] + "\"";
			else
				lbls += "|" + "\"" + descs[idx] + "\"";
			idx++;
		}
		retVal += "multibool, ";
		retVal += ids + ", ";
		retVal += defs + ", ";
		retVal += lbls + ", ";
		retVal += "\"" + super.getParamDesc() + "\", ";
		retVal += numCols;

		retVal += "}";

		return retVal;
	}

	@Override
	public void setValue(String paramId, Object val) {
		Boolean value = (val == null ? null : Boolean.FALSE);
		if( val != null ) {
			if(val instanceof Boolean)
				value = (Boolean)val;
			else
				value = Boolean.valueOf(val.toString());
		}
		super.setValue(paramId, value);
	}
	
}
