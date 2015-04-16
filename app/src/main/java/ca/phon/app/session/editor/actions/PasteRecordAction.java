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
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.KeyStroke;

import ca.phon.app.session.RecordTransferable;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.AddRecordEdit;
import ca.phon.session.Record;

/**
 * Paste record from system clipboard.
 */
public class PasteRecordAction extends SessionEditorAction {
	
	private final static Logger LOGGER = Logger
			.getLogger(PasteRecordAction.class.getName());

	private static final long serialVersionUID = 4581031841588720169L;

	private final static String CMD_NAME = "Paste record";
	
	private final static String SHORT_DESC = "Paste record from clipboard after current record";
	
	private final static String ICON = "";
	
	private final static KeyStroke KS = 
			KeyStroke.getKeyStroke(KeyEvent.VK_V,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK);
	
	public PasteRecordAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e1) {
		final Transferable clipboardContents = 
				Toolkit.getDefaultToolkit().getSystemClipboard().getContents(getEditor());
		if(clipboardContents == null) return;
		Object obj = null;
		try {
			obj = clipboardContents.getTransferData(RecordTransferable.FLAVOR);
		} catch (UnsupportedFlavorException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		final RecordTransferable recTrans = (obj != null ? (RecordTransferable)obj : null);
		if(recTrans != null) {
			final Record record = recTrans.getRecord();
			
			final int index = getEditor().getCurrentRecordIndex() + 1;
			
			final AddRecordEdit edit = new AddRecordEdit(getEditor(), record, index);
			getEditor().getUndoSupport().postEdit(edit);
		}
	}

}
