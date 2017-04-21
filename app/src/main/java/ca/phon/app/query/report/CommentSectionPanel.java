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

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.phon.query.report.io.CommentSection;

public class CommentSectionPanel extends SectionPanel<CommentSection> {
	
	/**
	 * Help text
	 */
	private final static String INFO_TEXT = 
		"<html><body>" +
		"<i>Comment</i>" +
		"<p>Outputs the text entered above into the report.</p>" +
		"<p>To create a table, prefix each line with '||' and separate cell contents with '|'.<br>" +
		"e.g., <pre>||cell1|cell2|cell3</p>" +
		"</body></html>";
	
	/** Text area */
	private JTextArea commentArea;

	public CommentSectionPanel(CommentSection section) {
		super(section);
		
		init();
	}
	
	private void init() {
		super.setInformationText(getClass().getName()+".info", INFO_TEXT);
		
		commentArea = new JTextArea();
		commentArea.setWrapStyleWord(true);
		commentArea.setText(getSection().getValue());
		commentArea.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				updateCommentText();
				
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				updateCommentText();
				
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				updateCommentText();
				
			}
			
			private void updateCommentText() {
				CommentSection comSection = getSection();
				comSection.setValue(commentArea.getText());
			}
			
		});
		
		// use a border layout
		JPanel innerPanel = new JPanel(new BorderLayout());
		innerPanel.setBorder(BorderFactory.createTitledBorder("Comment"));
		innerPanel.add(new JScrollPane(commentArea), BorderLayout.CENTER);
		
		add(innerPanel, BorderLayout.CENTER);
	}

}
