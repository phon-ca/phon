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
package ca.phon.app.query.report;

import ca.phon.query.report.io.ReportDesign;

import java.io.File;

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
