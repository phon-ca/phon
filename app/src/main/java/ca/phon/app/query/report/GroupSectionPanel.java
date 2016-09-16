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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.query.report.io.Group;

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
