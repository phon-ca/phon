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
package ca.phon.app.session.editor.view.common;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import ca.phon.app.session.editor.*;
import ca.phon.plugin.*;
import ca.phon.session.*;

/**
 * Create tier editors
 */
public class TierEditorFactory {

	public TierEditorFactory() {
		
	}
	
	/**
	 * Create a new tier editor for the given tier.
	 * 
	 * @param tier
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TierEditor createTierEditor(SessionEditor editor, TierDescription tierDescription,
			Tier<?> tier, int group) {
		TierEditor retVal = null;
		
		final Class<?> tierType = tier.getDeclaredType();
		
		final List<IPluginExtensionPoint<TierEditor>> extPts = 
				PluginManager.getInstance().getExtensionPoints(TierEditor.class);
		for(IPluginExtensionPoint<TierEditor> extPt:extPts) {
			final TierEditorInfo info = extPt.getClass().getAnnotation(TierEditorInfo.class);
			if(info != null) {
				if(info.type() == tierType) {
					if(info.tierName().length() > 0) {
						if(!info.tierName().equals(tier.getName())) continue;
					}
					retVal = extPt.getFactory().createObject(editor, tier, group);
					// don't continue to look use this editor
					if(info.tierName().equalsIgnoreCase(tier.getName())) {
						break;
					}
				}
			}
		}
		
		// create a generic tier editor
		if(retVal == null) {
			retVal = new GroupField(tier, group, !tierDescription.isGrouped());
		}
		
		installTierEditorActions(retVal.getEditorComponent());
		
		return retVal;
	}
	
	final String nextEditorActId = "next-editor";
	final Action nextEditorAct = new AbstractAction() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			FocusManager.getCurrentManager().focusNextComponent();
		}
		
	};
	
	final String prevEditorActId = "prev-editor";
	final Action prevEditorAct = new AbstractAction() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			FocusManager.getCurrentManager().focusPreviousComponent();
		}
		
	};
	final KeyStroke nextEditorKS = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
	final KeyStroke prevEditorKS = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK);
	
	/**
	 * Install tier editor action map onto given component.
	 * 
	 * @param comp
	 */
	private void installTierEditorActions(final JComponent comp) {
		final InputMap inputMap = comp.getInputMap(JComponent.WHEN_FOCUSED);
		final ActionMap actionMap = comp.getActionMap();
		
		// navigation
		actionMap.put(nextEditorActId, nextEditorAct);
		inputMap.put(nextEditorKS, nextEditorActId);
		
		actionMap.put(prevEditorActId, prevEditorAct);
		inputMap.put(prevEditorKS, prevEditorActId);
		
		comp.setActionMap(actionMap);
		comp.setInputMap(JComponent.WHEN_FOCUSED, inputMap);
	}
}
