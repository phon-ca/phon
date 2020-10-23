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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.record_data.*;
import ca.phon.session.Record;
import ca.phon.util.icons.*;

/**
 * Action for creating a new group in a record.  This command will
 * create a group <i>after</i> the current group.
 */
public class NewGroupCommand extends RecordDataEditorViewAction {
	
	private static final long serialVersionUID = 4424323795875330824L;
	
	private static final String CMD_NAME = "New group after current";
	
	private final static String ICON = "actions/group_add";
	
	public NewGroupCommand(RecordDataEditorView editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
	}
	
	public NewGroupCommand(RecordDataEditorView editor, Record record, int index) {
		this(editor);
		setRecord(record);
		setIndex(index);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final AddGroupEdit edit = new AddGroupEdit(getEditorView().getEditor(), getRecord(), getIndex()+1);
		super.getEditorView().getEditor().getUndoSupport().postEdit(edit);
	}
	
}
