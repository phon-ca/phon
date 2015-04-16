/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.ui.toast;

import java.awt.event.MouseEvent;

import javax.swing.Popup;

/**
 * Listens for display/close events on a Toast window.
 */
public interface ToastListener {
	
	/**
	 * Called when the Toast window is displayed.
	 * 
	 * @param toast the toast object 
	 * @param popup the toast popup
	 */
	public void startedToast(Toast toast, Popup window);
	
	/**
	 * Called when the Toast window is closed.
	 * 
	 * @param toast
	 * @param popup
	 */
	public void finishedToast(Toast toast, Popup popup);
	
	/**
	 * Called when the Toast window is clicked.
	 * 
	 * @param toast
	 * @param window
	 * @param mouseEvent
	 */
	public void clink(Toast toast, MouseEvent event);

}
