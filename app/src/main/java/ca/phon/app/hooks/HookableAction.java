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
package ca.phon.app.hooks;

import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

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
