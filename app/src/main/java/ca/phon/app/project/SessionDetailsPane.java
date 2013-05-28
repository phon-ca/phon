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

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import ca.phon.project.Project;

public class SessionDetailsPane extends JTextArea {
	
	private static final long serialVersionUID = -1625212681352543764L;

	/** The project */
	private final Project project;
	
	/** The document */
	private final SessionDetailsDocument document;
	
	/**
	 * Constructor
	 *
	 */
	public SessionDetailsPane(Project project) {
		super();
		
		this.project = project;
		
		this.setEditable(false);
		
		this.document = new SessionDetailsDocument();
		this.setDocument(document);
		
	}
	
	public void setSession(String corpus, String session) {
		document.setSession(corpus, session);
	}

	
	private class SessionDetailsDocument 
		extends DefaultStyledDocument {
		/** The corpus */
		private String corpus;
		
		/** The sessin */
		private String session;
		
		/** Constructor */
		public SessionDetailsDocument() {
			super();
			
			this.corpus = null;
			
			createStyles();
		}
		
		private void createStyles() {
			Style def = StyleContext.getDefaultStyleContext().
				getStyle(StyleContext.DEFAULT_STYLE);
			
			Style corpusName = addStyle("SessionName", def);
			StyleConstants.setBold(corpusName, true);
			StyleConstants.setFontSize(corpusName, 14);
			
			Style normal = addStyle("Normal", def);
		}
		
		public void updateDocument() {
			try {
				super.remove(0, getLength());
			} catch (BadLocationException e) {}
			if(corpus == null || session == null) {
				return;
			}
			
			try {
//				super.insertString(getLength(), session + "\n\n", getStyle("SessionName"));
				
//				SystemProperties validationData = 
//					project.getSessionValidationData();
			
				
//				String numRecordsProp = 
//					corpus + "." + session + ".numrecords";
				
				String numRecordsString = 
					"Number of records: " + 
					(project.numberOfRecordsInSession(corpus, session));
				
				super.insertString(getLength(), numRecordsString + "\n\n", getStyle("Normal"));
				
//				String lastModifiedProp = 
//					corpus + "." + session + ".modified";
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm");
				
				
				String lastModifiedString =
					"Last modified: " + 
					(sdf.format(project.getSessionModificationTime(corpus, session).toDate()));
				
				super.insertString(getLength(), lastModifiedString, getStyle("Normal"));
				
			} catch (BadLocationException e) {
				
			} catch (IOException e) {
				
			}
			
		}
		
		public void setSession(String corpus, String session) {
			this.corpus = corpus;
			this.session = session;
			updateDocument();
		}
	}
}
