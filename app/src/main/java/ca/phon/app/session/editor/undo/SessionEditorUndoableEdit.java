/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.session.editor.undo;

import java.lang.ref.WeakReference;
import java.util.Set;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoManager;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.session.Session;

/**
 * Base class for {@link SessionEditor} changes.  All changes to the
 * {@link Session} should go through the {@link UndoManager}.
 *
 */
public abstract class SessionEditorUndoableEdit extends AbstractUndoableEdit implements IExtendable {

	private static final long serialVersionUID = 7922388747133546800L;
	
	private final ExtensionSupport extSupport = new ExtensionSupport(SessionEditorUndoableEdit.class, this);

	/**
	 * Reference to the session editor
	 */
	private final WeakReference<SessionEditor> editorRef;
	
	/**
	 * Optional 'source' for edit.
	 */
	private Object source;
	
	/**
	 * Constructor
	 * 
	 * @param editor
	 */
	public SessionEditorUndoableEdit(SessionEditor editor) {
		super();
		this.editorRef = new WeakReference<SessionEditor>(editor);
		
		extSupport.initExtensions();
	}
	
	public SessionEditor getEditor() {
		return editorRef.get();
	}
	
	/**
	 * Helper method for firing events
	 * 
	 * @param eventName
	 * @param eventData
	 */
	public void queueEvent(String eventName, Object src, Object eventData) {
		final SessionEditor editor = getEditor();
		final EditorEventManager eventManager = editor.getEventManager();
		final EditorEvent event = new EditorEvent(eventName, src, eventData);
		eventManager.queueEvent(event);
	}
	
	/**
	 * Get the source for the edit.  This value is given to the resulting
	 * EditorEvents.  
	 * 
	 * During undo/redo operations, the source is always the value of
	 * getEditor().getUndoSupport().
	 * 
	 * @return the source
	 */
	public Object getSource() {
		return this.source;
	}
	
	/**
	 * Set the source for the edit.  This is the source passed to the
	 * initial execution of the edit.  Undo/redo operations always use
	 * the editor's undo support as the source.
	 * 
	 * @param the edit source
	 */
	public void setSource(Object source) {
		this.source = source;
	}
	
	@Override
	public void redo() {
		final Object oldSource = getSource();
	
		if(getEditor() != null) {
			setSource(getEditor().getUndoSupport());
			
			final Integer recordIdx = getExtension(Integer.class);
			if(recordIdx != null && getEditor().getCurrentRecordIndex() != recordIdx.intValue()) {
				getEditor().setCurrentRecordIndex(recordIdx.intValue());
			}
		}
		
		doIt();
		setSource(oldSource);
	}
	
	@Override
	public void undo() {
		if(getEditor() != null) {
			final Integer recordIdx = getExtension(Integer.class);
			if(recordIdx != null && getEditor().getCurrentRecordIndex() != recordIdx.intValue()) {
				getEditor().setCurrentRecordIndex(recordIdx.intValue());
			}
		}
	}
	
	/**
	 * 'Do' the specified action.  The method is called by the
	 * EditorUnsoSupport when new edits are posted.
	 * 
	 * 
	 */
	public abstract void doIt();

	@Override
	public boolean canRedo() {
		return true;
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public boolean isSignificant() {
		return true;
	}

	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}
	
}
