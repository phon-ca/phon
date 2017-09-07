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
package ca.phon.app.session.editor.actions;

import java.awt.Toolkit;
import java.awt.event.*;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.AddRecordEdit;
import ca.phon.session.*;
import ca.phon.util.icons.*;

public class DuplicateRecordAction extends SessionEditorAction {

	private static final long serialVersionUID = 8927870935233124123L;
	
	private final static String CMD_NAME = "Duplicate record";
	
	private final static String SHORT_DESC = "Duplicate record after current";
	
	private final static String ICON = "misc/record-duplicate";
	
	private final static KeyStroke KS = KeyStroke.getKeyStroke(
			KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

	public DuplicateRecordAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(ACCELERATOR_KEY, KS);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		final SessionFactory factory = SessionFactory.newFactory();
		final Record r = getEditor().currentRecord();
		if(r != null) {
			final Record dup = factory.cloneRecord(r);
			final AddRecordEdit edit = new AddRecordEdit(getEditor(), dup, getEditor().getCurrentRecordIndex()+1);
			getEditor().getUndoSupport().postEdit(edit);
		}
	}

}
