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
package ca.phon.app.session.editor.view.timeline.actions;

import ca.phon.app.session.editor.view.timeline.TimelineView;
import ca.phon.util.icons.*;

import java.awt.event.ActionEvent;

/**
 * Split current record interval.
 * 
 */
public class SplitRecordAction extends TimelineAction {
	
	public final static String TXT = "Split record";
	
	public final static String DESC = "Split record into two";
	
	public final static String ICON = "actions/group_split";
	
	public SplitRecordAction(TimelineView view) {
		super(view);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		getView().getRecordTier().beginSplitMode();
	}
	
}
