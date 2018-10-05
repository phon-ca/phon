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
package ca.phon.app.session.editor.view.record_data.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.undo.MergeGroupEdit;
import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.session.Record;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Action for merging two groups in a record.  This action will
 * merge the specified group with the one after it (if found)
 */
public class MergeGroupCommand extends RecordDataEditorViewAction {
	
	private static final long serialVersionUID = -2409569868503667376L;

	private final static String ICON = "actions/group_merge";

	private final RecordDataEditorView editor;
	
	public MergeGroupCommand(RecordDataEditorView editor) {
		super(editor);
		this.editor = editor;

		putValue(NAME, "Merge group with next");
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK));
	}

	public MergeGroupCommand(RecordDataEditorView editor, Record record, int index) {
		this(editor);
		
		setRecord(record);
		setIndex(index);
//		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Record r = getRecord();
		if(r == null) return;
		int idx = getIndex();
		if(idx < r.numberOfGroups() - 1) {
			final MergeGroupEdit edit = new MergeGroupEdit(getEditorView().getEditor(), r, idx);
			getEditorView().getEditor().getUndoSupport().postEdit(edit);
		}
	}
	
}
