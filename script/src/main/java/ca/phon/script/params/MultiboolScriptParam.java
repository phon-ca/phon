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

import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class MultiboolScriptParam extends ScriptParam {
	
	/** Descriptions */
	private String[] descs;
	
	/** Number of columns */
	private int numCols = 2;
	
	/** Checkboxes */
	private HashMap<String, JCheckBox> boxes = 
		new LinkedHashMap<String, JCheckBox>();
	
	private final JPanel panel;
	
	public MultiboolScriptParam(String[] ids, Boolean[] defaults, String[] descs, String desc, int numCols) {
		super(ids, defaults);
		
		setParamType("multibool");
		setParamDesc(desc);
		
		this.descs = descs;
		this.numCols = numCols;
		
		for(int i = 0; i < ids.length; i++) {
			JCheckBox box = new JCheckBox(descs[i]);
			box.setSelected(defaults[i]);
			box.addChangeListener(new CheckboxListener(ids[i]));
			
			boxes.put(ids[i], box);
		}
		
		panel = new JPanel(new BorderLayout()) {
			@Override
			public void setEnabled(boolean enabled) {
				super.setEnabled(enabled);
				for(String key:boxes.keySet()) {
					boxes.get(key).setEnabled(enabled);
				}
			}
		};
		panel.add(createPanel(), BorderLayout.CENTER);
	}

	private FormLayout createLayout() {
		String cols = "";
		for(int i = 0; i < numCols; i++) {
			cols += (i > 0 ? "," : "") + "pref, 5dlu";
		}
		FormLayout retVal = new FormLayout(cols, "");
		return retVal;
	}
	
	private JPanel createPanel() {
		DefaultFormBuilder builder = new DefaultFormBuilder(createLayout());
		
		int colIndex = 0;
		for(String id:getParamIds()) {
			if(colIndex == numCols) {
				builder.nextLine();
				colIndex = 0;
			}
			
			boxes.get(id).setSelected((Boolean)getValue(id));
			builder.append(boxes.get(id));
			
			colIndex++;
		}
		return builder.getPanel();
	}
	
	@Override
	public JComponent getEditorComponent() {
		for(String id:getParamIds()) {
			boxes.get(id).setSelected((Boolean)getValue(id));
		}
		return panel;
	}

	public JCheckBox getCheckbox(String id) {
		return boxes.get(id);
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

	private class CheckboxListener implements ChangeListener {
		
		/** The index */
		private String id = "";
		
		public CheckboxListener(String id) {
			super();
			this.id = id;
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			JCheckBox src = (JCheckBox)e.getSource();
			setValue(id, src.isSelected());
		}
		
	}
}
