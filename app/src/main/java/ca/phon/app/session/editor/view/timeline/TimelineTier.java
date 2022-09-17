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
package ca.phon.app.session.editor.view.timeline;

import ca.phon.app.session.editor.EditorView;
import ca.phon.media.TimeComponent;
import ca.phon.ui.menu.MenuBuilder;

public abstract class TimelineTier extends TimeComponent {

	private static final long serialVersionUID = 1L;

	public final TimelineView parentView;
	
	public TimelineTier(TimelineView parent) {
		super(parent.getTimeModel());
		
		this.parentView = parent;
	}

	public TimelineView getParentView() {
		return this.parentView;
	}
	
	public boolean isResizeable() {
		return true;
	}
	
	/**
	 * Setup context menu
	 */
	public abstract void setupContextMenu(MenuBuilder builder, boolean includeAccelerators);
	
	/**
	 * Called when the {@link EditorView} is closed
	 * 
	 */
	public abstract void onClose();
	
}
