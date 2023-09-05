package ca.phon.app.session.editor;

import ca.phon.app.project.ProjectFrame;
import ca.phon.project.Project;
import ca.phon.session.Participant;
import ca.phon.session.Session;
import ca.phon.session.Transcriber;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.menu.MenuManager;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Session editor window
 *
 */
public class SessionEditorWindow extends ProjectFrame  {

    private final SessionEditor sessionEditor;

    public SessionEditorWindow(Project project, Session session, Transcriber transcriber) {
        super(project);

        this.sessionEditor = new SessionEditor(project, session, transcriber);
        init();
    }

    private void init() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(this.sessionEditor, BorderLayout.CENTER);

        this.sessionEditor.addPropertyChangeListener("modified", e -> {
            setModified(sessionEditor.isModified());
            setTitle(sessionEditor.getTitle());
        });

        sessionEditor.getEventManager().registerActionForEvent(EditorEventType.ParticipantAdded, this::onParticipantListChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
        sessionEditor.getEventManager().registerActionForEvent(EditorEventType.ParticipantRemoved, this::onParticipantListChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
        sessionEditor.getEventManager().registerActionForEvent(EditorEventType.ParticipantChanged, this::onParticipantListChanged, EditorEventManager.RunOn.AWTEventDispatchThread);

        this.addWindowFocusListener(new SessionEditorModificationListener(this.sessionEditor));
    }

    private void onParticipantListChanged(EditorEvent<Participant> ee) {
        setJMenuBar(MenuManager.createWindowMenuBar(this));
    }

    public Session getSession() {
        return sessionEditor.getSession();
    }

    public SessionEditor getSessionEditor() {
        return this.sessionEditor;
    }

    /**
     * Setup editor window menu
     *
     * @param menuBar
     */
    @Override
    public void setJMenuBar(JMenuBar menuBar) {
        if(menuBar != null)
            getSessionEditor().setupMenu(menuBar);
        super.setJMenuBar(menuBar);
    }

    private void _dispose() {
        setVisible(false);
        CommonModuleFrame.getOpenWindows().remove(this);
        setJMenuBar(null);
        sessionEditor.getEventManager().shutdown();
        sessionEditor.getViewModel().cleanup();

        sessionEditor.getUndoManager().discardAllEdits();
//        sessionEditor.undoSupport.removeUndoableEditListener(undoListener);

//        eventManagerRef.set(null);
//        viewModelRef.set(null);
//        selectionModelRef.set(null);
//        dataModelRef.set(null);

        System.gc();

        super.dispose();
    }

    @Override
    public void dispose() {
        sessionEditor.getEventManager().registerActionForEvent(EditorEventType.EditorClosing, (ee) -> SwingUtilities.invokeLater(this::_dispose));
        // send out closing event
        final EditorEvent<Void> ee = new EditorEvent<>(EditorEventType.EditorClosing, this, null);
        sessionEditor.getEventManager().queueEvent(ee);
    }

    @Override
    public boolean saveData()
            throws IOException {
        return getSessionEditor().saveData();
    }

}
