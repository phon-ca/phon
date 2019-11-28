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
package ca.phon.app.session.editor;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;

/**
 * A view in the {@link SessionEditor}.  Each editor view
 * displays a different view of the current session/record data.
 * 
 * 
 */
public abstract class EditorView extends JPanel implements IExtendable {
	
	private static final long serialVersionUID = 907723403385573953L;
	
	private final ExtensionSupport extSupport = new ExtensionSupport(EditorView.class, this);

	/**
	 * Preferred dock position
	 */
	private DockPosition preferredDockPosition = DockPosition.CENTER;
	
	/**
	 * Editor view listeners
	 */
	private final EventListenerList listenerList = new EventListenerList();
	
	/**
	 * Parent editor
	 * 
	 */
	private final WeakReference<SessionEditor> editorRef;
	
	public EditorView(SessionEditor editor) {
		super();
		editorRef = new WeakReference<SessionEditor>(editor);
		extSupport.initExtensions();
	}
	
	/**
	 * Get the parent editor
	 * 
	 * @return
	 */
	public SessionEditor getEditor() {
		return editorRef.get();
	}
	
	/**
	 * View name
	 * 
	 * @return the view name
	 */
	public abstract String getName();
	
	/**
	 * View icon
	 * 
	 * @return view icon
	 */
	public abstract ImageIcon getIcon();
	
	/**
	 * Get the menu for the view (if any)
	 * 
	 * @return menu for the view or <code>null</code> if
	 *  this view does not have a menu
	 */
	public abstract JMenu getMenu();
	
	/**
	 * Called when the editor view is placed into the 
	 * view layout.
	 * 
	 */
	void onOpen() {
		for(EditorViewListener listener:getEditorViewListeners()) {
			listener.onOpened(this);
		}
	}
	
	/**
	 * Called when the close action is called on an editor view.
	 * 
	 */
	void onClose() {
		for(EditorViewListener listener:getEditorViewListeners()) {
			listener.onClosed(this);
		}
	}
	
	/**
	 * Called when the view is focused
	 */
	void onFocused() {
		for(EditorViewListener listener:getEditorViewListeners()) {
			listener.onFocused(this);
		}
	}
	
	/**
	 * Add editor view listener
	 * 
	 * @param listener
	 */
	public void addEditorViewListener(EditorViewListener listener) {
		listenerList.add(EditorViewListener.class, listener);
	}
	
	/**
	 * Remove editor view listener
	 * 
	 * @param listener
	 */
	public void removeEditorViewListner(EditorViewListener listener) {
		listenerList.remove(EditorViewListener.class, listener);
	}
	
	/**
	 * Get editor view listeners
	 * 
	 * @return editor view listeners
	 */
	public EditorViewListener[] getEditorViewListeners() {
		return listenerList.getListeners(EditorViewListener.class);
	}
	
	/**
	 * Gets the preferred dock position for the view
	 * 
	 * @return preferred dock position
	 */
	public DockPosition getPreferredDockPosition() {
		return this.preferredDockPosition;
	}
	
	/**
	 * Sets the preferred dock position for the view
	 * 
	 * @param dockPosition
	 */
	public void setPreferredDockPosition(DockPosition dockPosition) {
		this.preferredDockPosition = dockPosition;
	}

	public void initExtensions() {
		extSupport.initExtensions();
	}

	@Override
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}
	
}
