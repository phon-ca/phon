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
package ca.phon.app.session.editor.view.tier_management.actions;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.actions.*;
import ca.phon.app.session.editor.view.tier_management.*;

public abstract class TierManagementAction extends SessionEditorAction {

	private static final long serialVersionUID = -4527188790604848801L;

	private TierOrderingEditorView view;
	
	public TierManagementAction(SessionEditor editor, TierOrderingEditorView view) {
		super(editor);
		this.view = view;
	}
	
	public TierOrderingEditorView getView() {
		return this.view;
	}

}
