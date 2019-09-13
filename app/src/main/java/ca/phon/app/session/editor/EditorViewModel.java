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

import java.awt.Container;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.MenuElement;

/**
 * <p>View model for the {@link SessionEditor}.  This class
 * is responsible for creating and placing any {@link EditorView}s.</p>
 *
 */
public interface EditorViewModel {
	
	
	/**
	 * Remove all view references and cleanup resources.
	 * 
	 * 
	 */
	public void cleanup();
	
	/**
	 * Get the view root for the editor.  This is the component
	 * that will be displayed in the root pane of the editor
	 * window.
	 * 
	 * @return the root container for the editor
	 */
	public Container getRoot();
	
	/**
	 * Get the view specified by the given name.
	 * 
	 * @param viewName
	 * @return specified editor view or <code>null</code> if
	 *  view was not found
	 */
	public EditorView getView(String viewName);
	
	/**
	 * Get dynamic view component with the given name. This
	 * method is used for getting references to components
	 * displayed with the {@link #showDynamicFloatingDockable(String, JComponent, int, int, int, int)} 
	 * method.
	 * 
	 * @param viewName
	 * @return specified view component or <code>null</code> if
	 *  view was not found
	 */
	public JComponent getDynamicView(String viewName);
	
	/**
	 * Get the icon associated with the view.  This information
	 * comes from the view itself if the view is loaded, otherwise
	 * information is obtained from the EditorViewInfo annotation
	 * on the extension point.
	 * 
	 * @return view icon
	 */
	public ImageIcon getViewIcon(String viewName);

	/**
	 * Get the list of view names handeled by this
	 * model
	 * 
	 * @return list of available view names
	 */
	public Set<String> getViewNames();
	
	/**
	 * Get the set of available views organized by
	 * category.
	 * 
	 * @return editor views by category
	 */
	public Map<EditorViewCategory, List<String>> getViewsByCategory();
	
	/**
	 * Show the specified view.
	 * 
	 * @param viewName
	 */
	public void showView(String viewName);
	
	/**
	 * Get currently focused view
	 * 
	 * @param focused view or <code>null</code>
	 */
	public EditorView getFocusedView();
	
	/**
	 * Get the close action for a view
	 * 
	 * @param viewName
	 * 
	 * @return the close action for the view
	 */
	public Action getCloseAction(String viewView);
	
	/**
	 * Is the specified view showing
	 * 
	 * @return <code>true</code> if the given view is part
	 *  of the current dock control, <code>false</code> otherwise
	 */
	public boolean isShowing(String viewName);
	
	/**
	 * Show the specified view as a new dynamic floating
	 * view.  These views are <b>not</b> saved in layouts.
	 *
	 * @param title
	 * @param comp
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public void showDynamicFloatingDockable(String title, JComponent comp, int x, int y, int w, int h);
	
	/**
	 * Hide the specified view
	 * 
	 * @param viewName
	 */
	public void hideView(String viewName);
	
	/**
	 * Setup windows using the given perspective
	 * 
	 * @param editorPerspective
	 */
	public void setupWindows(RecordEditorPerspective editorPerspective);
	
	/**
	 * Setup views based on the given perspective 
	 * 
	 * @param editorPerspective
	 */
	public void applyPerspective(RecordEditorPerspective editorPerspective);
	
	/**
	 * Save the current view perspective as the specified editor 
	 * perspective
	 * 
	 * @param editorPerspective
	 */
	public void savePerspective(RecordEditorPerspective editorPerspective);
	
	/**
	 * Remove prespective from dock control
	 * 
	 * @param editorPerspective
	 */
	public void removePrespective(RecordEditorPerspective editorPerspective);
	
	/**
	 * Setup view menu
	 * 
	 * @param menuElement
	 */
	public void setupViewMenu(MenuElement menuElement);
	
	/**
	 * Setup perspective menu.
	 * 
	 * @param menuElement
	 */
	public void setupPerspectiveMenu(MenuElement menuElement);
	
	/**
	 * Setup layout menu.
	 * 
	 * @param menuElement
	 */
	public void setupLayoutMenu(MenuElement menuElement);
	

}
