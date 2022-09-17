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
package ca.phon.app.session.editor.view.record_data.actions;

import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.session.Record;

import javax.swing.*;

public abstract class RecordDataEditorViewAction extends AbstractAction {

	private static final long serialVersionUID = 7623979692728491952L;

	private final RecordDataEditorView editor;
	
	private Record record;
	
	private int index = -1;
	
	public RecordDataEditorViewAction(RecordDataEditorView editor) {
		super();
		this.editor = editor;
	}
	
	public RecordDataEditorView getEditorView() {
		return this.editor;
	}
	
	public Record getRecord() {
		Record r = record;
		if(r == null)
			r = editor.getEditor().currentRecord();
		return r;
	}

	public void setRecord(Record record) {
		this.record = record;
	}

	/**
	 * Returns the group index.  If not set, will return the current
	 * group index as indicated by the editor view or last group
	 * in the record.
	 * 
	 * @return
	 */
	public int getIndex() {
		int idx = (index < 0 ? 
				(editor.currentGroupIndex() < 0 ? getRecord().numberOfGroups() - 1 : editor.currentGroupIndex())
				: index);
		return idx;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
