/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import ca.phon.app.session.editor.undo.AddGroupEdit;
import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.session.Record;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

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
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
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
