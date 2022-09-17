/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.*;
import ca.phon.extensions.*;
import ca.phon.session.Session;

import javax.swing.undo.*;
import java.lang.ref.WeakReference;
import java.util.Set;

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
