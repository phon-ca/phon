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
package ca.phon.app.session.editor.search.actions;

import java.awt.event.*;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.view.record_data.*;

public class ReplaceAction extends FindAndReplaceAction {

	private static final long serialVersionUID = 2574281425626924879L;
	
	private final static String CMD_NAME = "Replace";
	
	private final static String SHORT_DESC = "Replace";
	
	private boolean andFind = false;
	
	public ReplaceAction(SessionEditor editor, FindAndReplacePanel view, boolean andFind) {
		super(editor, view);
		
		this.andFind = andFind;
		
		putValue(NAME, CMD_NAME + (andFind ? " and find" : ""));
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		getFindAndReplacePanel().replace();
		if(andFind) {
			getFindAndReplacePanel().findNext();
		}
	}

}
