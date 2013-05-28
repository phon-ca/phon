/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.app.project;

import java.util.logging.Logger;

import javax.swing.JTextArea;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import ca.phon.project.Project;


public class CorpusDetailsPane extends JTextArea {
	
	private static final long serialVersionUID = 7236346469179415466L;

	private final static Logger LOGGER = Logger.getLogger(CorpusDetailsPane.class.getName());
	
	/** The project */
	private final Project project;
	
	/** The document */
	private final CorpusDetailsDocument document;
	
	
	/**
	 * Constructor
	 */
	public CorpusDetailsPane(Project project) {
		super();
		
		document = new CorpusDetailsDocument();
//		document.addDocumentListener(new DocumentListener() {
//
//			public void changedUpdate(DocumentEvent e) {
//				updateCorpusDescription();
//			}
//
//			public void insertUpdate(DocumentEvent e) {
//				updateCorpusDescription();
//			}
//
//			public void removeUpdate(DocumentEvent e) {
//				updateCorpusDescription();
//			}
//			
//		});
		
		this.setDocument(document);
		
		this.project = project;
	}
	
	private void updateCorpusDescription() {
		String corpusDescription = null;
		try {
			corpusDescription = 
				document.getText(document.getDescriptionStartIndex(), 
						document.getLength()-document.getDescriptionStartIndex());
		} catch (BadLocationException e) {
			LOGGER.warning(e.getMessage());
			return;
		}
		
		project.setCorpusDescription(document.corpus, corpusDescription);

	}
	
	public void setCorpus(String corpus) {
		document.setCorpus(corpus);
	}
	
	private class CorpusDetailsDocument 
		extends DefaultStyledDocument {
		/** The corpus */
		private String corpus;
		
		/** The edit position */
		private int firstEditPosition = Integer.MAX_VALUE;
		
		/** Constructor */
		public CorpusDetailsDocument() {
			super();
			
			this.corpus = null;
			
			createStyles();
		}
		
		public int getDescriptionStartIndex() {
			return this.firstEditPosition;
		}
		
		@Override
		public void insertString(int offs, String val, AttributeSet attr) 
			throws BadLocationException {
			if(offs >= firstEditPosition) {
				super.insertString(offs, val, attr);
				updateCorpusDescription();
			} else
				return;
		}
		
		@Override
		public void remove(int offs, int len)
			throws BadLocationException {
			if(offs >= firstEditPosition) {
				super.remove(offs, len);
				updateCorpusDescription();
			} else
				return;
		}
		
		private void createStyles() {
			Style def = StyleContext.getDefaultStyleContext().
				getStyle(StyleContext.DEFAULT_STYLE);
			
			Style corpusName = addStyle("CorpusName", def);
			StyleConstants.setBold(corpusName, true);
			StyleConstants.setFontSize(corpusName, 14);
			
			Style normal = addStyle("Normal", def);
		}
		
		public void updateDocument() {
			try {
				super.remove(0, getLength());
			} catch (BadLocationException e) {}
			if(corpus == null) {
				return;
			}
			
			try {
//				super.insertString(getLength(), corpus + "\n\n", getStyle("CorpusName"));
				
				String numSessionsString = 
					"Number of sessions: " + project.getCorpusSessions(corpus).size();
				
				String description = "Description: \n";
				String description2 = 
					project.getCorpusDescription(corpus);
				
				super.insertString(getLength(), numSessionsString + "\n\n", getStyle("Normal"));
				
				super.insertString(getLength(), description, getStyle("Normal"));
				
				firstEditPosition = getLength();
				
				super.insertString(getLength(), description2, getStyle("Normal"));
			} catch (BadLocationException e) {
				
			}
			
		}
		
		public void setCorpus(String corpus) {
			this.corpus = corpus;
			updateDocument();
		}
		
		
	}
}
