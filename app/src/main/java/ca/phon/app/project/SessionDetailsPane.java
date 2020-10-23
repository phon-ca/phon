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
package ca.phon.app.project;

import java.io.*;
import java.time.*;
import java.time.format.*;

import javax.swing.*;

import ca.phon.project.*;
import ca.phon.worker.*;

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
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd@h:mma");

				final ZonedDateTime time = project.getSessionModificationTime(corpus, session);

				sb.append("Number of records: ").append(numRecords).append("\n\n");
				sb.append("Last modified: ").append(formatter.format(time));
			} catch (IOException e) {

			}
			SwingUtilities.invokeLater(onEDT);
		};
		PhonWorker.getInstance().invokeLater(inBg);
	}

}
