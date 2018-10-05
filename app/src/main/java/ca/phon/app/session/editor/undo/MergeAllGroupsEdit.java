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
package ca.phon.app.session.editor.undo;

import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Record;

/**
 * Merge all groups for the given record into a single group.
 * 
 */
public class MergeAllGroupsEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = 3524587790171851452L;

	private Record record;

	final CompoundEdit cmpEdit = new CompoundEdit();
	
	public MergeAllGroupsEdit(SessionEditor editor, Record record) {
		super(editor);
		
		this.record = record;
	}
	
	public Record getRecord() {
		return this.record;
	}
	
	@Override
	public void undo() throws CannotUndoException {
		cmpEdit.undo();
	}
	
	@Override
	public String getPresentationName() {
		return "merge all groups";
	}

	@Override
	public void doIt() {
		final int origNumGroups = record.numberOfGroups();
		if(origNumGroups <= 1) return; // nothing to do
		
		while(record.numberOfGroups() > 1) {
			int numGroups = record.numberOfGroups();
			final MergeGroupEdit mergeEdit = new MergeGroupEdit(getEditor(), getRecord(), 0);
			mergeEdit.doIt();
			if(numGroups == record.numberOfGroups()) {
				// nothing happened - avoid looping forever
				break;
			}
			
			cmpEdit.addEdit(mergeEdit);
		}
		cmpEdit.end();
	}

}
