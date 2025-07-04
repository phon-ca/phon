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

import ca.phon.query.report.io.Group;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GroupSectionPanel extends SectionPanel<Group> {
	
	/**
	 * Help text
	 */
	private final static String INFO_TEXT = 
		"<html><body>" +
		"<i>Report Section</i>" +
		"<p>A report section may contain one or more report elements for each result set.</p>" +
		"<p>Session (including participant) information may be included at the beginning of each iteration of the report section.</p>" +
		"</body></html>";
	
	/*
	 * UI
	 */
	private JCheckBox printSessionInfoBox;
	
	private JCheckBox printParticipantInfoBox;
	
	public GroupSectionPanel(Group section) {
		super(section);
		
		init();
	}
	
	private void init() {
		super.setInformationText(getClass().getName()+".info", INFO_TEXT);
		Group gt = getSection();
		
		FormLayout layout = new FormLayout(
				"5dlu, fill:pref:grow",
				"pref, pref");
		JPanel panel = new JPanel(layout);
		CellConstraints cc = new CellConstraints();
		
		printSessionInfoBox = new JCheckBox("Include session information");
		printSessionInfoBox.setSelected(gt.isPrintSessionHeader());
		printSessionInfoBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				getSection().setPrintSessionHeader(printSessionInfoBox.isSelected());
				printParticipantInfoBox.setEnabled(printSessionInfoBox.isSelected());
			}
		});
		
		printParticipantInfoBox = new JCheckBox("Include participant information");
		printParticipantInfoBox.setSelected(gt.isPrintParticipantInformation());
		printParticipantInfoBox.setEnabled(printSessionInfoBox.isSelected());
		printParticipantInfoBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				getSection().setPrintParticipantInformation(printParticipantInfoBox.isSelected());
			}
		});
		
		panel.add(printSessionInfoBox, cc.xyw(1,1,2));
		panel.add(printParticipantInfoBox, cc.xy(2, 2));
		
		panel.setBorder(BorderFactory.createTitledBorder("Options"));
		add(panel, BorderLayout.CENTER);
	}
	
}
