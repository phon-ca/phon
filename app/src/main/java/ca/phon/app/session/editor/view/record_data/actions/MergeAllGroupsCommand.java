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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.undo.MergeAllGroupsEdit;
import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.session.Record;
import ca.phon.util.OSInfo;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class MergeAllGroupsCommand extends RecordDataEditorViewAction {

	private static final long serialVersionUID = 7674607989381956414L;

	private final static String ICON = "actions/group_merge";

	private final RecordDataEditorView editor;
	
	public MergeAllGroupsCommand(RecordDataEditorView editor) {
		super(editor);
		this.editor = editor;

		putValue(NAME, "Merge all groups");
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, 
				(OSInfo.isMacOs() ? Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() : KeyEvent.ALT_DOWN_MASK ) | KeyEvent.SHIFT_DOWN_MASK));
	}
		
	@Override
	public void actionPerformed(ActionEvent e) {
		final Record r = getRecord();
		editor.getEditor().getUndoSupport().beginUpdate();
		if(r.numberOfGroups() > 1) {
						
			final MergeAllGroupsEdit edit = new MergeAllGroupsEdit(editor.getEditor(), editor.getEditor().currentRecord());			
			getEditorView().getEditor().getUndoSupport().postEdit(edit);
		}
		editor.getEditor().getUndoSupport().endUpdate();
	}

}
