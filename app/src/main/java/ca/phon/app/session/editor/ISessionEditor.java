package ca.phon.app.session.editor;

import ca.phon.app.session.editor.undo.SessionEditorUndoSupport;
import ca.phon.session.Session;

import javax.swing.undo.UndoManager;

/**
 * Interface for session editors.  Implementing this interface allows reuse of actions/edits.
 */
public interface ISessionEditor {

    /**
     * Get session
     *
     * @return session being edited
     */
    public Session getSession();

    /**
     * Get media model
     *
     * @return session media model
     */
    public SessionMediaModel getMediaModel();

    /**
     * Get editor event manager.
     *
     * @return editor event manager
     */
    public EditorEventManager getEventManager();

    /**
     * Get editor undo support
     *
     * @return editor undo support
     */
    public SessionEditorUndoSupport getUndoSupport();

    /**
     * Get undo manager
     *
     * @return undo manager
     */
    public UndoManager getUndoManager();

    /**
     * Get current record index
     *
     * @return current record index, -1 if no current record
     */
    public int getCurrentRecordIndex();

    /**
     * Set current record index
     *
     * @param recordIndex
     * @throws ArrayIndexOutOfBoundsException
     */
    public void setCurrentRecordIndex(int recordIndex);

}
