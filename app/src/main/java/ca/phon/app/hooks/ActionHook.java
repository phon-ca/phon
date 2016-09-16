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
