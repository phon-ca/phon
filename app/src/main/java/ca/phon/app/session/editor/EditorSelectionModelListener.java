package ca.phon.app.session.editor;

public interface EditorSelectionModelListener {
	
	/**
	 * Called when a new selection has been added to the model.
	 * 
	 * @param model
	 * @param selection
	 */
	public void selectionAdded(EditorSelectionModel model, SessionEditorSelection
			selection);
	
	/**
	 * Called when a new selection has been set (clearing other selections)
	 * 
	 * @param model
	 * @param selection
	 */
	public void selectionSet(EditorSelectionModel model, SessionEditorSelection selection);
	
	/**
	 * Called when all selections have been cleared.
	 * 
	 * @param model
	 */
	public void selectionsCleared(EditorSelectionModel model);
	
}
