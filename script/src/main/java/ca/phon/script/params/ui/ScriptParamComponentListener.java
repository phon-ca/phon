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
package ca.phon.script.params.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import javax.swing.JComponent;

import ca.phon.script.params.ScriptParam;

public class ScriptParamComponentListener implements PropertyChangeListener {
	
	/**
	 * Component
	 */
	private final WeakReference<JComponent> compRef;
	
	public ScriptParamComponentListener(JComponent comp) {
		super();
		this.compRef= new WeakReference<JComponent>(comp);
	}
	
	public JComponent getComponent() {
		return this.compRef.get();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		final ScriptParam param = ScriptParam.class.cast(evt.getSource());
		final String evtName = evt.getPropertyName();
		if(evtName.equals(ScriptParam.ENABLED_PROP)) {
			getComponent().setEnabled(param.isEnabled());
		} else if(evtName.equals(ScriptParam.VISIBLE_PROP)) {
			getComponent().setVisible(param.getVisible());
		}
	}

}
