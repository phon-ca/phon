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
package ca.phon.app.hooks;

import java.awt.event.*;

/**
 * Interface used for {@link HookableAction}s.
 *
 * @param A the action type being hooked
 */
public interface ActionHook<A extends HookableAction> {
	
	/**
	 * Return the action type.
	 * 
	 * @return hookable action class
	 */
	public Class<? extends A> getActionType();

	/**
	 * Method called before the {@link HookableAction#hookableActionPerformed(ActionEvent)} method
	 * is called.  This method may pre-empt the action by returning <code>true</code>.
	 * 
	 * @param action
	 * @param ae
	 * 
	 * @return <code>true</code> if the action should be cancelled, <code>false</code>
	 *  otherwise
	 */
	public boolean beforeAction(A action, ActionEvent ae);
	
	/**
	 * Method called afteer the {@link HookableAction#hookableActionPerformed(ActionEvent)} method
	 * is called.
	 * 
	 * @param action
	 * @param ae
	 * 
	 */
	public void afterAction(A action, ActionEvent ae);
	
}
