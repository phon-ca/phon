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
package ca.phon.app.opgraph.editor.actions.view;

import java.awt.event.*;

import ca.phon.app.opgraph.editor.*;
import ca.phon.app.opgraph.editor.actions.*;

public class ToggleViewAction extends OpgraphEditorAction {

	private static final long serialVersionUID = 2681118382414781020L;

	public final static String DESC = "Toggle view";
	
	public ToggleViewAction(OpgraphEditor editor, String viewName) {
		super(editor);
		
		putValue(NAME, viewName);
		putValue(SHORT_DESCRIPTION, DESC + " " + viewName);
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		final String viewName = (String)getValue(NAME);
		if(getEditor().isViewVisible(viewName)) {
			getEditor().hideView(viewName);
		} else {
			getEditor().showView(viewName);
		}
	}

}
