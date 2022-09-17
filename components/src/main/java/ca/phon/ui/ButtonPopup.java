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
package ca.phon.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.beans.*;
import java.lang.ref.WeakReference;

public class ButtonPopup {
	
	/**
	 * Property key for popup visibility
	 */
	public final static String POPUP_VISIBLE = "popupVisible";
	
	private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
	
	private WeakReference<Object> popupRef;
	
	private JPopupMenu menu;
	
	public ButtonPopup(JComponent c) {
		popupRef = new WeakReference<Object>(c);
	}
	
	public ButtonPopup(JPopupMenu popupMenu) {
		popupRef = new WeakReference<Object>(popupMenu);
	}
	
	public Object getPopupObj() {
		return popupRef.get();
	}
	
	public JPopupMenu getMenu() {
		if(this.menu == null) {
			Object popupObj = getPopupObj();
			if(popupObj == null) return null;
			menu = (popupObj instanceof JPopupMenu ? (JPopupMenu)popupObj : new JPopupMenu());
			if(!(popupObj instanceof JPopupMenu)) {
				menu.add((JComponent)popupObj);
			}
		}
		return this.menu;
	}
	
	public void hide() {
		getMenu().setVisible(false);
	}
	
	public void show(JComponent c) {
		Object popupObj = getPopupObj();
		if(popupObj == null) return;
		
		JPopupMenu menu = getMenu();
		if(menu == null) return;
		
		propSupport.firePropertyChange(POPUP_VISIBLE, false, true);
		menu.show(c, 0, c.getHeight());
		menu.addPopupMenuListener(new PopupMenuListener() {
			
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}
			
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				propSupport.firePropertyChange(POPUP_VISIBLE, true, false);
				menu.removePopupMenuListener(this);
			}
			
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				
			}
			
		});
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propSupport.removePropertyChangeListener(listener);
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
		return propSupport.getPropertyChangeListeners();
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propSupport.removePropertyChangeListener(propertyName, listener);
	}

	public boolean hasListeners(String propertyName) {
		return propSupport.hasListeners(propertyName);
	}
	
}