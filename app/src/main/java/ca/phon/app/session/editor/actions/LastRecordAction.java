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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.ui.nativedialogs.OSInfo;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Move {@link SessionEditor} to last record.
 *
 */
public class LastRecordAction extends SessionEditorAction {

	private static final long serialVersionUID = -5103061811540777442L;
	
	private final static String CMD_NAME = "Last record";
	
	private final static String SHORT_DESC = "Go to last record";
	
	private final static String ICON = "actions/go-last";
	
	private final static KeyStroke KS = 
			KeyStroke.getKeyStroke(KeyEvent.VK_N, (OSInfo.isMacOs() ? KeyEvent.CTRL_MASK : KeyEvent.ALT_MASK) | KeyEvent.SHIFT_MASK );

	public LastRecordAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		getEditor().setCurrentRecordIndex(getEditor().getDataModel().getRecordCount()-1);
	}

}
