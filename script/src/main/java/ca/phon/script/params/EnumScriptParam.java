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

import java.io.IOException;
import java.io.Serializable;

public class EnumScriptParam extends ScriptParam {

	private String[] choices;
	
	public EnumScriptParam(String id, String desc, int def, String[] choices) {
		super();
		
		this.choices = choices;
		setParamType("enum");
		setParamDesc(desc);
		setValue(id, null);
		setDefaultValue(id, new ReturnValue(choices[def], def));
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