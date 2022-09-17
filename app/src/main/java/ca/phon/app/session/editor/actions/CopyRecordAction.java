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
package ca.phon.app.session.editor.actions;

import ca.phon.app.session.RecordsTransferable;
import ca.phon.app.session.editor.SessionEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_MASK);

	public CopyRecordAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final SessionEditor editor = getEditor();

		final RecordsTransferable clipboardContents = new RecordsTransferable(editor.getSession(), new int[]{editor.getCurrentRecordIndex()});
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(clipboardContents, editor);
	}

}
