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

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import ca.phon.query.report.io.*;

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
