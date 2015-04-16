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
package ca.phon.app.session.editor.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.RecordTransferable;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Record;
import ca.phon.session.SessionFactory;

/**
 * Copy record data into system clipboard. 
 */
public class CopyRecordAction extends SessionEditorAction {

	private static final long serialVersionUID = 2539545211879667310L;
	
	private final static String CMD_NAME = "Copy record";
	
	private final static String SHORT_DESC = "Copy record to clipboard";
	
	private final static String ICON = "";
	
	private final static KeyStroke KS = 
			KeyStroke.getKeyStroke(KeyEvent.VK_C,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK);

	public CopyRecordAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final SessionEditor editor = getEditor();
		final Record record = editor.currentRecord();
		
		final SessionFactory factory = SessionFactory.newFactory();
		final Record copiedRecord = factory.cloneRecord(record);
		
		final RecordTransferable clipboardContents = new RecordTransferable(copiedRecord);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(clipboardContents, editor);
	}

}
