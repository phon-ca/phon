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
