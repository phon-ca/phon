package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorViewModel;
import ca.phon.session.Session;

public class ShowHideViewEdit extends SessionUndoableEdit {

    private static final long serialVersionUID = 1L;

    private final EditorViewModel viewModel;

    private final String viewName;

    private final boolean show;

    /**
     * Constructor
     *
     * @param session
     * @param editorEventManager
     */
    public ShowHideViewEdit(Session session, EditorEventManager editorEventManager,
                            EditorViewModel viewModel, String viewName, boolean show) {
        super(session, editorEventManager);
        this.viewModel = viewModel;
        this.viewName = viewName;
        this.show = show;
    }

    @Override
    public void doIt() {
        if(show && !viewModel.isShowing(viewName)) {
            viewModel.showView(viewName);
            fireEditorLayoutChanged();
        } else if(!show && viewModel.isShowing(viewName)) {
            viewModel.hideView(viewName);
            fireEditorLayoutChanged();
        }
    }

    @Override
    public void undo() {
        if(show && viewModel.isShowing(viewName)) {
            viewModel.hideView(viewName);
            fireEditorLayoutChanged();
        } else if(!show && !viewModel.isShowing(viewName)) {
            viewModel.showView(viewName);
            fireEditorLayoutChanged();
        }
    }

    private void fireEditorLayoutChanged() {
        final var ee = new EditorEvent<>(EditorEventType.EditorLayoutChanged, null, null);
        getEditorEventManager().queueEvent(ee);
    }

    @Override
    public String getRedoPresentationName() {
        return (show ? "Redo show " : "Redo hide ") + viewName + " view";
    }

    @Override
    public String getUndoPresentationName() {
        return (show ? "Undo show " : "Undo hide ") + viewName + " view";
    }
}
