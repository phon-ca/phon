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
package ca.phon.media.sampled.actions;

import java.awt.event.ActionEvent;

import ca.phon.media.sampled.PCMSegmentView;

public class PlaySelectionAction extends PCMSegmentViewAction {

	private static final long serialVersionUID = -7273435862236125102L;

	public final static String TXT = "Play selection";
	
	public final static String DESC = "Play current selection";
	
	public PlaySelectionAction(PCMSegmentView view) {
		super(view);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getView().playSelection();
	}

}
