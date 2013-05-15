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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.JComboBox;
import javax.swing.JComponent;

public class EnumScriptParam extends ScriptParam {

	/** The editor comp */
	private final JComboBox comboBox;
	
	private String[] choices;
	
	private String _id;
	
	private final ItemListener listener = new ItemListener() {

		@Override
		public void itemStateChanged(ItemEvent e) {
			String value = comboBox.getSelectedItem().toString();
			int index = comboBox.getSelectedIndex();
			setValue(_id, new ReturnValue(value, index));
		}
		
	};
	
	public EnumScriptParam(String id, String desc, int def, String[] choices) {
		super();
		
		this.choices = choices;
		_id = id;
		setParamType("enum");
//		setParamId(id);
		setParamDesc(desc);
		setValue(id, null);
		setDefaultValue(id, new ReturnValue(choices[def], def));
		
		comboBox = new JComboBox(choices);
		comboBox.setSelectedItem(getValue(id).toString());
		comboBox.addItemListener(listener);
	}
	
	public ReturnValue[] getChoices() {
		ReturnValue[] retVal = new ReturnValue[choices.length];
		for(int i = 0; i < choices.length; i++) {
			retVal[i] = new ReturnValue(choices[i], i);
		}
		return retVal;
	}
	
	@Override
	public JComponent getEditorComponent() {
		comboBox.setSelectedItem(getValue(_id));
		return comboBox;
	}
	
	@Override
	public String getStringRepresentation() {
		String retVal = "{";

		String id = super.getParamIds().iterator().next();
		retVal += "enum, ";
		retVal += id + ", ";
		String choices = null;
		for(int i = 0; i < comboBox.getItemCount(); i++) {
			String choice = (String)comboBox.getItemAt(i);
			if(choices == null)
				choices = "\"" + choice + "\"";
			else
				choices += "|\"" + choice + "\"";
		}
		retVal += choices + ", ";
		retVal += ((ReturnValue)super.getDefaultValue(id)).getIndex() + ", ";
		retVal += "\"" + super.getParamDesc() + "\"";

		retVal += "}";

		return retVal;
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
		
		if(selectedValue != null) {
			comboBox.removeItemListener(listener);
			comboBox.setSelectedIndex(selectedValue.index);
			comboBox.addItemListener(listener);
		}
	}

	public static class ReturnValue implements Serializable {
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