/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
