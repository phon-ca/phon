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
import ca.phon.session.Record;

import javax.swing.undo.CannotUndoException;
import java.awt.*;

public class AddGroupEdit extends SessionEditorUndoableEdit {
	
	/**
	 * tier
	 */
	private final Record record;
	
	/** 
	 * group index
	 */
	private final int groupIndex;
	
	public AddGroupEdit(SessionEditor editor, Record record, int groupIndex) {
		super(editor);
		this.record = record;
		this.groupIndex = groupIndex;
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo add group";
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo add group";
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		
		record.removeGroup(groupIndex);

		final EditorEvent<Void> ee = new EditorEvent<>(EditorEventType.GroupListChange, (Component) getSource(), null);
		getEditor().getEventManager().queueEvent(ee);
	}

	@Override
	public void doIt() {
		record.addGroup(groupIndex);

		final EditorEvent<Void> ee = new EditorEvent<>(EditorEventType.GroupListChange, (Component) getSource(), null);
		getEditor().getEventManager().queueEvent(ee);
	}

}
