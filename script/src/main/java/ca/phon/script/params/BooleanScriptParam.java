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

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BooleanScriptParam extends ScriptParam {
	
	/** The editor comp */
	private final JCheckBox editorComp = new JCheckBox();
	
	private String _id;
	
	public BooleanScriptParam(String id, String labelText, String desc, Boolean defaultValue) {
		super();
		
		_id = id;
		setParamType("bool");
		setParamDesc(desc);
		setValue(id, defaultValue);
		setDefaultValue(id, defaultValue);
		
		editorComp.setText(labelText);
		editorComp.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Boolean newValue = editorComp.isSelected();
				setValue(_id, newValue);
			}
			
		});
	}

	@Override
	public JComponent getEditorComponent() {
		editorComp.setSelected((Boolean)getValue(_id));
		return editorComp;
	}

	@Override
	public String getStringRepresentation() {
		String retVal = "{";

		String id = super.getParamIds().iterator().next();
		retVal += "bool, ";
		retVal += id + ", ";
		retVal += super.getDefaultValue(id).toString() + ", ";
		retVal += "\"" + editorComp.getText() + "\"";
		retVal += ", \"" + super.getParamDesc() + "\"";

		retVal += "}";

		return retVal;
	}

	@Override
	public void setValue(String paramId, Object val) {
		if(val == null) {
			super.setValue(paramId, Boolean.FALSE);
		} else {
			if(val instanceof Boolean) {
				super.setValue(paramId, val);
			} else {
				Boolean v = Boolean.parseBoolean(val.toString());
				super.setValue(paramId, v);
			}
		}
	}
	
	
}
