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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.undo.RemoveGroupEdit;
import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

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
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK | KeyEvent.ALT_MASK));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// don't delete last group!
		if(getRecord().numberOfGroups() == 1) return;
		final RemoveGroupEdit edit = new RemoveGroupEdit(getEditorView().getEditor(), getRecord(), getIndex());
		getEditorView().getEditor().getUndoSupport().postEdit(edit);
	}
	
}
