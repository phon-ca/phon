package ca.phon.app.session.editor;

/**
 * Listen for changes to editor views including showing, hiding, minimizing, maximizing, normalizing, externalizing,
 * and focusing.
 */
public interface EditorViewModelListener {

    public void viewShown(String viewName);

    public void viewHidden(String viewName);

    public void viewMinimized(String viewName);

    public void viewMaximized(String viewName);

    public void viewNormalized(String viewName);

    public void viewExternalized(String viewName);

    public void viewFocused(String viewName);

}
