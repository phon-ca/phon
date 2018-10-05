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

import ca.phon.app.session.editor.undo.SplitGroupEdit;
import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.session.Record;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class SplitGroupCommand extends RecordDataEditorViewAction {
	
	private final static String ICON = "actions/group_split";
	
	private int wordIndex = -1;

	public SplitGroupCommand(RecordDataEditorView editor) {
		super(editor);
		
		putValue(NAME, "Split group");
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK));
	}
	
	public int getWordIndex() {
		return wordIndex;
	}

	public void setWordIndex(int wordIndex) {
		this.wordIndex = wordIndex;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Record r = getRecord();
		if(r == null) return;
		
		final int grp = getIndex();
		
		int wIdx = getWordIndex();
		if(wIdx < 0) {
			wIdx = getEditorView().currentWordIndex();
		}
		final SplitGroupEdit edit = new SplitGroupEdit(getEditorView().getEditor(), r, grp, wIdx);
		getEditorView().getEditor().getUndoSupport().postEdit(edit);
	}

}
