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
package ca.phon.app.session.editor.view.check.actions;

import java.awt.event.*;

import ca.phon.app.session.editor.view.check.*;
import ca.phon.util.icons.*;

public class SessionCheckRefreshAction extends SessionCheckViewAction {

	public SessionCheckRefreshAction(SessionCheckView view) {
		super(view);
		
		putValue(SessionCheckViewAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/reload", IconSize.SMALL));
		putValue(SessionCheckViewAction.NAME, "Refresh");
		putValue(SessionCheckViewAction.SHORT_DESCRIPTION, "Refresh session check");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		SessionCheckView view = getView();
		if(view != null) {
			view.refresh();
		}
	}

}
