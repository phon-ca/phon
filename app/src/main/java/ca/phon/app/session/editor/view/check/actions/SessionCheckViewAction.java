/*
 * Copyright (C) 2012-2019 Gregory Hedlund & Yvan Rose
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

import java.lang.ref.WeakReference;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.session.editor.view.check.SessionCheckView;

public abstract class SessionCheckViewAction extends HookableAction {
	
	private static final long serialVersionUID = -1426864802203354389L;

	private WeakReference<SessionCheckView> viewRef;
	
	public SessionCheckViewAction(SessionCheckView view) {
		super();
		
		viewRef = new WeakReference<>(view);
	}
	
	public SessionCheckView getView() {
		return this.viewRef.get();
	}

}
