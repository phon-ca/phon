package ca.phon.ui;

import java.beans.*;
import java.lang.ref.*;

import javax.swing.*;
import javax.swing.event.*;

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