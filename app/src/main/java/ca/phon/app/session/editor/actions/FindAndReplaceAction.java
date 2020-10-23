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

import java.awt.event.*;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.view.record_data.*;
import ca.phon.util.icons.*;

public class FindAndReplaceAction extends SessionEditorAction {

	private static final long serialVersionUID = -548370051934852629L;

	private final static String TXT = "Find & Replace";
	
	private final static String DESC = "Show Record Data view with Find & Replace UI visible";
	
	private final static String ICON_NAME = "actions/edit-find-replace";
	
	public FindAndReplaceAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON_NAME, IconSize.SMALL));
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		SessionEditor editor = getEditor();
		if(!editor.getViewModel().isShowing(RecordDataEditorView.VIEW_NAME)) {
			editor.getViewModel().showView(RecordDataEditorView.VIEW_NAME);
		}
		((RecordDataEditorView)editor.getViewModel().getView(RecordDataEditorView.VIEW_NAME)).setFindAndReplaceVisible(true);
	}

}
