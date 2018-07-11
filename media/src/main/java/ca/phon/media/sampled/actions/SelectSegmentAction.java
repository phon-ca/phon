/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
