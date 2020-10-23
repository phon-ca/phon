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
package ca.phon.app.session.editor.view.find_and_replace.actions;

import java.awt.event.*;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.view.find_and_replace.*;

public class FindPrevAction extends FindAndReplaceAction {

	private static final long serialVersionUID = -3610764173181048977L;
	
	private final static String CMD_NAME = "Find previous";
	
	private final static String SHORT_DESC = "Find previous";

	public FindPrevAction(SessionEditor editor, FindAndReplaceEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		getView().findPrev();
	}

}
