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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JComponent;

public abstract class ScriptParam {
	
	/** The param type */
	private String paramType;

	/** The description */
	private String paramDesc;
	
	/** The paramid's avd values */
	private HashMap<String, Object> values =
		 new LinkedHashMap<String, Object>();
	
	/** The default values */
	private HashMap<String, Object> defValues = 
		new LinkedHashMap<String, Object>();
	
	/**
	 * Listeners
	 */
	private List<ParamListener> listeners = 
			new ArrayList<ParamListener>();
	
	public void addListener(ParamListener listener) {
		if(!listeners.contains(listener))
			listeners.add(listener);
	}
	
	public void removeListener(ParamListener listener) {
		listeners.remove(listener);
	}
	
	public void fireParamValueChanged(String paramid, Object oldvalue, Object newvalue) {
		if(oldvalue != null) {
			if(oldvalue.equals(newvalue)) return;
		}
		ParamListener[] handlers = listeners.toArray(new ParamListener[0]);
		for(ParamListener handler:handlers)
			handler.onParamValueChanged(paramid, oldvalue, newvalue);
	}
	
	public ScriptParam() {
		this(new String[0], new Object[0]);
	}
	
	public ScriptParam(String[] ids, Object[] defaultValues) {
		super();
		
		for(int i = 0; i < ids.length; i++) {
			setValue(ids[i], null);
			setDefaultValue(ids[i], defaultValues[i]);
		}
	}
	
	public Collection<String> getParamIds() {
		return values.keySet();
	}

	public String getParamType() {
		return paramType;
	}

	public void setParamType(String paramType) {
		this.paramType = paramType;
	}

	public String getParamDesc() {
		return paramDesc;
	}

	public void setParamDesc(String paramDesc) {
		this.paramDesc = paramDesc;
	}
	
	public Object getValue(String paramId) {
		Object retVal = 
			(values.get(paramId) == null ? getDefaultValue(paramId) : values.get(paramId));
		return retVal;
	}
	
	public void setValue(String paramId, Object val) {
		Object oldval = getValue(paramId);
		values.put(paramId, val);
		fireParamValueChanged(paramId, oldval, val);
	}

	public Object getDefaultValue(String paramId) {
		return defValues.get(paramId);
	}

	public void setDefaultValue(String paramId, Object defaultValue) {
		defValues.put(paramId, defaultValue);
	}
	
	public abstract JComponent getEditorComponent();

	public boolean hasChanged() {
		boolean retVal = false;

		for(String id:getParamIds()) {
			Object def = getDefaultValue(id);
			Object val = getValue(id);
			retVal |= !def.equals(val);

		}

		return retVal;
	}

	/**
	 * Get the string representation of this
	 * param.
	 */
	public abstract String getStringRepresentation();

	@Override
	public String toString() {
		return getStringRepresentation();
	}
	
	/**
	 * Copies any values from oldParams which have matching 
	 * ids in newParams.
	 * 
	 * @param oldParams
	 * @param newParams
	 */
	public static void copyParams(ScriptParam[] oldParams, ScriptParam[] newParams) {
		for(ScriptParam sp:newParams) {
			for(String pId:sp.getParamIds()) {
				
				// find the matching pId in oldParams (if exists)
				ScriptParam oldParam = null;
				for(ScriptParam oldSp:oldParams) {
					if(oldSp.getParamIds().contains(pId)) {
						oldParam = oldSp;
					}
				}
				
				// make sure the type of the param has not changed
				if(oldParam != null && oldParam.getParamType().equals(sp.getParamType())) {
					sp.setValue(pId, oldParam.getValue(pId));
				}
			}
		}
	}

	/**
	 * Convert the list of script params into groups.
	 */
	public static List<ScriptParamGroup> getParamGroups(ScriptParam[] params) {
		List<ScriptParamGroup> retVal = new ArrayList<ScriptParamGroup>();

		ScriptParam currentSep = null;
		List<ScriptParam> currentGrp = null;
		for(int i = 0; i < params.length; i++) {
			ScriptParam current = params[i];
			if(current.getParamType().equals("separator")) {
				if(currentGrp != null) {
					ScriptParamGroup grp = new ScriptParamGroup(currentSep, currentGrp);
					retVal.add(grp);
				}
				currentSep = current;
				currentGrp = new ArrayList<ScriptParam>();
			} else {
				currentGrp.add(current);
			}
		}

		if(currentGrp != null && currentGrp.size() > 0) {
			ScriptParamGroup grp = new ScriptParamGroup(currentSep, currentGrp);
			retVal.add(grp);
		}

		return retVal;
	}
	
//	public void setupParams(Scriptable scope) {
//		Object wrappedObj = Context.javaToJS(getValue(), scope);
//		ScriptableObject.putProperty(scope, getParamId(), wrappedObj);
//	}
//	
//	public void saveParams(Query q) {
//		q.setParam(getParamId(), getValue());
//	}
}
