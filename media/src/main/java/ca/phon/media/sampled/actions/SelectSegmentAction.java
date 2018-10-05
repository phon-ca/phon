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
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.media.sampled.PCMSegmentView;

public class SelectSegmentAction extends PCMSegmentViewAction {

	private static final long serialVersionUID = -8882931703864142319L;

	public final static String TXT = "Select segment";
	public final static String DESC = "Change segment to current selection";
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
	
	public SelectSegmentAction(PCMSegmentView view) {
		super(view);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final PCMSegmentView view = getView();
		
		final float selectionStart = view.getSelectionStart();
		final float selectionLength = view.getSelectionLength();
		
		if(selectionStart > 0 && selectionLength > 0) {
			// turn off UI updates
			view.setValuesAdusting(true);
			view.setSelectionStart(0.0f);
			view.setSelectionLength(0.0f);
			view.setSegmentStart(selectionStart);
			
			// turn on UI updates for last change
			view.setValuesAdusting(false);
			view.setSegmentLength(selectionLength);
		}
	}

}
