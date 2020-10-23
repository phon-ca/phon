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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.record_data.*;
import ca.phon.util.icons.*;

/**
 * Removes specified group for all tiers.
 */
public class DeleteGroupCommand extends RecordDataEditorViewAction {
	
	private static final long serialVersionUID = 7588345374191473003L;

	private final static String CMD_NAME = "Delete current group";
	
	private final static String ICON = "actions/group_remove";
	
	public DeleteGroupCommand(RecordDataEditorView editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_MASK | KeyEvent.ALT_MASK));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// don't delete last group!
		if(getRecord().numberOfGroups() == 1) return;
		final RemoveGroupEdit edit = new RemoveGroupEdit(getEditorView().getEditor(), getRecord(), getIndex());
		getEditorView().getEditor().getUndoSupport().postEdit(edit);
	}
	
}
