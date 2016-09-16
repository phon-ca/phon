/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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
package ca.phon.app.session.editor.view.common;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.FocusManager;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.session.Tier;
import ca.phon.session.TierDescription;

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
