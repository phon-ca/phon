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
package ca.phon.script.params.ui;

import java.beans.*;
import java.lang.ref.*;

import javax.swing.*;

import ca.phon.script.params.*;

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
