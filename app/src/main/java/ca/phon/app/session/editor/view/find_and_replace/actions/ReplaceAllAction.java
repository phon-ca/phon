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
package ca.phon.app.session.editor.view.find_and_replace.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.find_and_replace.FindAndReplaceEditorView;

import java.awt.event.ActionEvent;

public class ReplaceAllAction extends FindAndReplaceAction {

	private static final long serialVersionUID = 4752402371749797345L;

	private final static String CMD_NAME = "Replace all";
	
	private final static String SHORT_DESC = "Replace all occurrences";
	
	public ReplaceAllAction(SessionEditor editor, FindAndReplaceEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		getView().replaceAll();
	}

}
