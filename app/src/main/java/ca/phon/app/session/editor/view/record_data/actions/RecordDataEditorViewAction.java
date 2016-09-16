/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.session.editor.view.record_data.actions;

import javax.swing.AbstractAction;

import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.session.Record;

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
