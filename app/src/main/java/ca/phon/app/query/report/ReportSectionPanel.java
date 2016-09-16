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
package ca.phon.app.query.report;

import java.io.File;

import ca.phon.query.report.io.ReportDesign;

public class ReportSectionPanel extends SectionPanel<ReportDesign> {
	
	/**
	 * Help text
	 */
	private final static String INFO_TEXT = 
		"<html><p><i>Report Outline</i></p><p>To add/remove sections to the report, use the up " +
		"and down buttons in the report outline.</p>" +
		"<p>Use the 'Result Sets' tab to select result sets displayed in the report.</p>" +
		"</html>";

	
	public ReportSectionPanel(ReportDesign section) {
		super(section);
		
		init();
	}
	
	private void init() {
		// get absolute locations of icons
		String addImgRelPath = 
			"data" + File.separator + "icons" +
			File.separator + "16x16" + File.separator + 
			"actions" + File.separator + "list-add.png";
		File addImgFile = new File(addImgRelPath);
		String addImgURI = addImgFile.toURI().toASCIIString();
		
		String removeImgRelPath = 
			"data" + File.separator + "icons" +
			File.separator + "16x16" + File.separator + 
			"actions" + File.separator + "list-remove.png";
		File remImgFile = new File(removeImgRelPath);
		String remImgURI = remImgFile.toURI().toASCIIString();
		
		String infoTxt = INFO_TEXT.replaceAll("\\$\\{section_add_img\\}", addImgURI)
								  .replaceAll("\\$\\{section_remove_img\\}", remImgURI);
		
		super.setInformationText(getClass().getName()+".info", infoTxt);
	}

}
