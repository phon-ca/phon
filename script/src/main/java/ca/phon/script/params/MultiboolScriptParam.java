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
package ca.phon.script.params;

import java.util.Iterator;

public class MultiboolScriptParam extends ScriptParam {
	
	/** ids */
	private String[] ids;
	
	/** Descriptions */
	private String[] descs;
	
	/** Enabled status */
	private boolean enabled[];
	
	/** Visiblity status */
	private boolean visible[];
	
	/** Number of columns */
	private int numCols = 2;
	
	public MultiboolScriptParam(String[] ids, Boolean[] defaults, String[] descs, String desc, int numCols) {
		super(ids, defaults);
		
		this.ids = ids;
		enabled = new boolean[ids.length];
		visible = new boolean[ids.length];
		for(int i = 0; i < ids.length; i++) {
			enabled[i] = true;
			visible[i] = true;
		}
		
		setParamType("multibool");
		setParamDesc(desc);
		
		this.descs = descs;
		this.numCols = numCols;
	}
	
	/**
	 * The number of columns
	 * @return
	 */
	public int getCols() {
		return this.numCols;
	}
	
	/**
	 * Number of options
	 * @return
	 */
	public int getNumberOfOptions() {
		return this.ids.length;
	}
	
	/**
	 * Set enabled status of specific options
	 * 
	 * @param option
	 * @param enabled
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public void setEnabled(int option, boolean enabled) {
		final boolean oldVal = this.enabled[option];
		this.enabled[option] = enabled;
		final String propName = ids[option] + ".enabled";
		super.propSupport.firePropertyChange(propName, oldVal, enabled);
	}
	
	/**
	 * Get enabled status of option
	 * 
	 * @param option
	 * @return enabled status
	 */
	public boolean isEnabled(int option) {
		return this.enabled[option];
	}
	
	/**
	 * Set visible status of specific options
	 * 
	 * @param option
	 * @param enabled
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public void setVisible(int option, boolean visible) {
		final boolean oldVal = this.visible[option];
		this.visible[option] = visible;
		final String propName = ids[option] + ".visible";
		super.propSupport.firePropertyChange(propName, oldVal, visible);
	}
	
	/**
	 * Get visible status of specific options
	 * 
	 * @param option
	 * @return
	 */
	public boolean isVisible(int option) {
		return this.visible[option];
	}
	
	/**
	 * Get text for specific option
	 * 
	 * @param option
	 * @return option text
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public String getOptionText(int option) {
		return descs[option];
	}
	
	/**
	 * Get the id for the specified option
	 * 
	 * @param option
	 * @return option id
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public String getOptionId(int option) {
		return ids[option];
	}
	
	/**
	 * Get the index of the given option id
	 * 
	 * @param id
	 * 
	 * @return index
	 */
	public int getOptionIndex(String id) {
		int retVal = -1;
		for(int i = 0; i < ids.length; i++) {
			final String v = ids[i];
			if(v.equals(id)) {
				retVal = i;
				break;
			}
		}
		return retVal;
	}
	
	/**
	 * Get option text for specific param id
	 * @param paramId
	 * @return
	 */
	public String getOptionText(String paramId) {
		String retVal = "";
		for(int i = 0; i < ids.length; i++) {
			if(ids[i].equals(paramId)) {
				retVal = descs[i];
				break;
			}
		}
		return retVal;
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
