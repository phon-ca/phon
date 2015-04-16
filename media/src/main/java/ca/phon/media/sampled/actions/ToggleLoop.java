/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import ca.phon.media.sampled.PCMSegmentView;

public class ToggleLoop extends PCMSegmentViewAction {

	private static final long serialVersionUID = 3781234856731539939L;

	public final static String TXT = "Loop";
	
	public final static String DESC = "Loop playback";
	
	public ToggleLoop(PCMSegmentView view) {
		super(view);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	@Override
	public Object getValue(String key) {
		if(key.equals(SELECTED_KEY)) {
			return getView().isLoop();
		} else {
			return super.getValue(key);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		boolean loop = getView().isLoop();
		getView().setLoop(!loop);
	}

}
