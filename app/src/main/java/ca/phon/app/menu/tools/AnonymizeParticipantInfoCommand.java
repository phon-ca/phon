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
package ca.phon.app.menu.tools;

import javax.swing.Action;

import ca.phon.app.project.AnonymizeParticipantInfoEP;
import ca.phon.plugin.PluginAction;

public class AnonymizeParticipantInfoCommand extends PluginAction {

	private static final long serialVersionUID = -3731528615504791889L;

	public AnonymizeParticipantInfoCommand() {
		super(AnonymizeParticipantInfoEP.EP_NAME);
		putValue(Action.NAME, "Strip Participant Information");
		putValue(Action.SHORT_DESCRIPTION, "Remove participant information from sessions");
	}
	
}
