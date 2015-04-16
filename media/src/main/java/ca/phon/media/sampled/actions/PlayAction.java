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
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.media.sampled.PCMSegmentView;

public class PlayAction extends PCMSegmentViewAction {

	private static final long serialVersionUID = 3592378313027362147L;
	
	public final static String TXT = "Play segment/selection";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
	
	public PlayAction(PCMSegmentView view) {
		super(view);
		
		putValue(NAME, TXT);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getView().play();
	}

}
