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
package ca.phon.app.hooks;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;

import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;

/**
 * 
 */
public abstract class HookableAction extends AbstractAction {

	private static final long serialVersionUID = -77335181664938946L;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public final void actionPerformed(ActionEvent ae) {
		final List<IPluginExtensionPoint<ActionHook>> startupHookPts =
				PluginManager.getInstance().getExtensionPoints(ActionHook.class);
		final Class<? extends HookableAction> myClass = getClass();
		boolean doAction = true;
		for(IPluginExtensionPoint<ActionHook> actionHookExt:startupHookPts) {
			final ActionHook<HookableAction> actionHook = actionHookExt.getFactory().createObject();
			if(actionHook.getActionType() == myClass) {
				doAction &= !actionHook.beforeAction(this, ae);
			}
			if(!doAction) break;
		}
		
		if(doAction)
			hookableActionPerformed(ae);
		
		for(IPluginExtensionPoint<ActionHook> actionHookExt:startupHookPts) {
			final ActionHook<HookableAction> actionHook = actionHookExt.getFactory().createObject();
			if(actionHook.getActionType() == myClass) {
				actionHook.afterAction(this, ae);
			}
		}
	}
	
	/**
	 * Method which should be implemented instead of 
	 * actionPerformed.
	 * 
	 * @param action event
	 */
	public abstract void hookableActionPerformed(ActionEvent ae);
	
}
