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
