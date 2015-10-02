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
package ca.phon.app.project;

import java.io.IOException;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ca.phon.project.Project;
import ca.phon.worker.PhonWorker;

public class SessionDetailsPane extends JTextArea {
	
	private static final long serialVersionUID = -1625212681352543764L;

	/** The project */
	private final Project project;
	
	/**
	 * Constructor
	 *
	 */
	public SessionDetailsPane(Project project) {
		super();
		
		this.project = project;
		
		this.setEditable(false);
	}
	
	public void setSession(String corpus, String session) {
		updateText(corpus, session);
	}
	
	public void updateText(final String corpus, final String session) {
		final StringBuffer sb = new StringBuffer();
		final Runnable onEDT = () -> {
			setText(sb.toString());
		};
		final Runnable inBg = () -> {
			try {
				int numRecords = project.numberOfRecordsInSession(corpus, session);
				DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd@K:ma");
				
				sb.append("Number of records: ").append(numRecords).append("\n\n");
				sb.append("Last modified: ").append(formatter.print(project.getSessionModificationTime(corpus, session)));
			} catch (IOException e) {
				
			}
			SwingUtilities.invokeLater(onEDT);
		};
		PhonWorker.getInstance().invokeLater(inBg);
	}
	
}
