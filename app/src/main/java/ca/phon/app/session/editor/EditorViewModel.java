package ca.phon.app.session.editor;

import java.awt.Container;
import java.util.List;
import java.util.Set;

/**
 * <p>View model for the {@link SessionEditor}.  This class
 * is responsible for creating and placing any {@link EditorView}s.</p>
 *
 */
public interface EditorViewModel {
	
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
	 */
	public EditorView getView(String viewName);

	/**
	 * Get the list of view names handeled by this
	 * model
	 * 
	 * @return list of available view names
	 */
	public Set<String> getViewNames();
	
	/**
	 * Show the specified view.
	 * 
	 * @param viewName
	 */
	public void showView(String viewName);
	
	/**
	 * Hide the specified view
	 * 
	 * @param viewName
	 */
	public void hideView(String viewName);
}
