/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.query.report;

import ca.phon.query.report.io.ParamSection;

public class ParamSectionPanel extends SectionPanel<ParamSection> {
	
	/**
	 * Help text
	 */
	private final static String INFO_TEXT = 
		"<html><body>" +
		"<i>Parameter List</i>" +
		"<p>Outputs the list of parameters and values used for the query.</p>" +
		"</body></html>";
	
	public ParamSectionPanel(ParamSection section) {
		super(section);
		init();
	}
	
	private void init() {
		super.setInformationText(getClass().getName()+".info", INFO_TEXT);
	}
	
}
