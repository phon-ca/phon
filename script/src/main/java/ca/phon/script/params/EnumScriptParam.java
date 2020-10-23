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
package ca.phon.script.params;

import java.io.*;

public class EnumScriptParam extends ScriptParam {

	private String[] choices;
	
	/**
	 * Type should be either 'combobox' or 'radiobutton'.
	 * Will default to 'combo'
	 */
	private final static String DEFAULT_TYPE = "combobox";
	private String type = DEFAULT_TYPE;
	
	private int columns = 1;
	
	public EnumScriptParam(String id, String desc, int def, String[] choices) {
		this(id, desc, def, choices, DEFAULT_TYPE, 1);
	}
	
	public EnumScriptParam(String id, String desc, int def, String[] choices, String type, int columns) {
		super();
		
		this.choices = choices;
		setParamType("enum");
		setParamDesc(desc);
		setValue(id, null);
		setDefaultValue(id, new ReturnValue(choices[def], def));
		setType(type);
		setColumns(columns);
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}
	
	public void setColumns(int columns) {
		this.columns = columns;
	}
	
	public int getColumns() {
		return this.columns;
	}
	
	public ReturnValue[] getChoices() {
		ReturnValue[] retVal = new ReturnValue[choices.length];
		for(int i = 0; i < choices.length; i++) {
			retVal[i] = new ReturnValue(choices[i], i);
		}
		return retVal;
	}
	
	public int getDefaultChoice() {
		final String paramId = getParamId();
		final ReturnValue defVal = (ReturnValue)getDefaultValue(paramId);
		if(defVal != null) {
			return defVal.index;
		} else {
			return 0;
		}
	}
	
	@Override
	public String getStringRepresentation() {
		final StringBuilder builder = new StringBuilder();
		builder.append("{");
		
		final String id = super.getParamIds().iterator().next();
		builder.append("enum, ");
		builder.append(id);
		builder.append(", ");
		
		for(int i = 0; i < choices.length; i++) {
			final String choice = choices[i];
			if(i > 0) builder.append("|");
			builder.append("\"");
			builder.append(choice);
			builder.append("\"");
		}
		
		final ReturnValue defValue = (ReturnValue)super.getDefaultValue(id);
		builder.append(", ");
		builder.append(defValue.getIndex());
		builder.append("\"");
		builder.append(super.getParamDesc());
		builder.append("\"");
		
		builder.append("}");

		return builder.toString();
	}

	@Override
	public void setValue(String paramId, Object val) {
		ReturnValue selectedValue = (ReturnValue)getDefaultValue(paramId);
		if(val != null) {
			if(val instanceof ReturnValue) {
				selectedValue = (ReturnValue)val;
			} else {
				if(val instanceof Integer) {
					int idx = (Integer)val;
					if(idx >= 0 && idx < getChoices().length)
						selectedValue = getChoices()[idx];
				} else {
					String vStr = val.toString();
					for(ReturnValue choice:getChoices()) {
						if(choice.value.equals(vStr)) {
							selectedValue = choice;
							break;
						}
					}
				}
			}
		}
		
		super.setValue(paramId, selectedValue);
	}

	public static class ReturnValue implements Serializable {
		
		private static final long serialVersionUID = -6369233483886788366L;
		
		private String value;
		private int index;
		
		public ReturnValue(String value, int index) {
			super();
			
			this.value = value;
			this.index = index;
		}
		
		public int getIndex() {
			return index;
		}
		
		public void setIndex(int ind) {
			this.index = ind;
		}
		
		@Override
		public String toString() {
			return value;
		}
		
		private void writeObject(java.io.ObjectOutputStream out)
	     throws IOException {
			out.writeInt(this.index);
			out.writeUTF(this.value);
		}
		
	    private void readObject(java.io.ObjectInputStream in)
	     throws IOException, ClassNotFoundException {
	    	index = in.readInt();
	    	value = in.readUTF();
	    }

	}
}