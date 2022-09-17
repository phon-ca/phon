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
package ca.phon.app.project.mergewizard;

import ca.phon.app.log.*;
import ca.phon.app.project.SessionMerger;
import ca.phon.project.Project;
import ca.phon.session.*;
import ca.phon.session.filter.RecordFilter;
import ca.phon.session.format.DateFormatter;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.wizard.*;
import ca.phon.worker.PhonWorker;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.*;

/**
 * Merge one or more session into a new session.
 *
 */
public class DeriveSessionWizard extends WizardFrame {

	private static final long serialVersionUID = -4619604190124079327L;

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(DeriveSessionWizard.class.getName());

	/*
	 * Steps
	 */
	private MergeSessionStep1 step1;

	private MergeSessionStep2 step2;

	private WizardStep mergeStep;

	private BufferPanel console;

	public DeriveSessionWizard(Project project) {
		super("Phon : " + project.getName() + " : Derive Session");
		setWindowName("Derive Session");

		super.putExtension(Project.class, project);

		init();
	}

	private Project getProject() {
		return getExtension(Project.class);
	}

	private void init() {
		btnCancel.setText("Close");
		btnFinish.setVisible(false);

		step1 = new MergeSessionStep1(getProject());
		step1.setNextStep(0);
		addWizardStep(step1);

		getRootPane().setDefaultButton(btnNext);
	}

	private WizardStep createMergeStep() {
		JPanel importPanel = new JPanel(new BorderLayout());

		DialogHeader importHeader = new DialogHeader("Derive Session", "Merge data from one or more sessions into a new session.");
		importPanel.add(importHeader, BorderLayout.NORTH);

		JPanel consolePanel = new JPanel(new BorderLayout());

		console = new BufferPanel("Derive Session");
		consolePanel.add(console, BorderLayout.CENTER);

		importPanel.add(consolePanel, BorderLayout.CENTER);

		return super.addWizardStep(importPanel);
	}

	@Override
	protected void next() {
		if(super.getCurrentStep() == step1) {
			// setup step2
			if(step2 == null) {
				step2 = new MergeSessionStep2(getProject(), step1.getSelectedSessions());
				step2.setPrevStep(0);
				step2.setNextStep(2);
				addWizardStep(step2);

				mergeStep = createMergeStep();
				mergeStep.setPrevStep(1);
				mergeStep.setNextStep(-1);
			} else {
				step2.setSelectedSessions(step1.getSelectedSessions());
			}

			step1.setNextStep(1);
		} else if(super.getCurrentStep() == step2) {
			// start merge task
			startMerge();
		}
		super.next();
	}

	private void startMerge() {
		PhonWorker worker = PhonWorker.createWorker();
		worker.setName("Session Merger");
		worker.setFinishWhenQueueEmpty(true);


		Runnable toRun = new Runnable() {
			@Override
			public void run() {
				Runnable turnOffBack = new Runnable() {
					@Override
					public void run() {
						btnBack.setEnabled(false);
						btnCancel.setEnabled(false);
					}
				};
				Runnable turnOnBack = new Runnable() {
					@Override
					public void run() {
						btnBack.setEnabled(true);
						btnCancel.setEnabled(true);
					}
				};
				SwingUtilities.invokeLater(turnOffBack);

				try {
					doMerge();
				} catch (IOException e) {
					// TODO show user message
				}

				SwingUtilities.invokeLater(turnOnBack);
			}
		};
		worker.invokeLater(toRun);

		worker.start();
	}

	/**
	 * Perform the merge.
	 */
	private void doMerge() throws IOException {
		final PrintWriter out = new PrintWriter(new OutputStreamWriter(console.getLogBuffer().getStdOutStream(), "UTF-8"));
		String corpus = step1.getMergedCorpusName();
		String session = step1.getMergedSessionName();

		List<SessionPath> sessions = step1.getSelectedSessions();
		Collections.sort(sessions);

		final Project project = getProject();
		// first make sure we have a corpus
		if(!project.getCorpora().contains(step1.getMergedCorpusName())) {

			out.println("Creating corpus '" + corpus + "'");
			out.flush();
			try {
				project.addCorpus(corpus, "");
			} catch (IOException e) {
				out.println(e.getLocalizedMessage());
				out.flush();
				LogUtil.severe( e.getLocalizedMessage(), e);
				return;
			}

		}

		// check session name
		if(project.getCorpusSessions(corpus).contains(session)) {
			out.println("A session with name '" + corpus + "." + session + "' already exists.");
			out.flush();
			return;
		}

		// create the new session
		out.println("Creating session '" + corpus + "." + session + "'");
		out.flush();
		final SessionFactory factory = SessionFactory.newFactory();
		try {
			final SessionMerger merger = new SessionMerger(project);

			final Session mergedSession = factory.createSession(corpus, session);
			step1.getSelectedParticipants().forEach( (p) -> {
				merger.addParticipant(p);
				if(p != Participant.UNKNOWN) {
					mergedSession.addParticipant(p);
				}
			} );

			merger.setMergedSession(mergedSession);

			final DateFormatter pdf = new DateFormatter();
			// merge sessions
			// keep track of session/media to see if we can sucessfully copy the data over
			String mergedDate = null;
			boolean checkDate = true;
			String mergedMedia = null;
			boolean checkMedia = true;
			for(SessionPath loc:sessions) {
				final Session t = step2.getSessionAtLocation(loc);
				final RecordFilter filter = step2.getFilterForLocation(loc);

				out.println("Merging data from session '" + loc.getCorpus() + "." + loc.getSession() + "'");
				out.flush();
				if(checkDate) {
					String tDate = pdf.format(t.getDate());
					if(mergedDate == null) {
						mergedDate = tDate;
					} else {
						if(!mergedDate.equals(tDate)) {
							out.println("Session dates do not match, setting merged session date to today.");
							out.flush();
							mergedDate = pdf.format(LocalDate.now());
							checkDate = false;
						}
					}
				}

				if(checkMedia) {
					if(mergedMedia == null) {
						mergedMedia = t.getMediaLocation();
					} else {
						if(!mergedMedia.equals(t.getMediaLocation())) {
							out.println("Session media locations do not match, leaving media field blank.");
							out.flush();
							mergedMedia = "";
							checkMedia = false;
						}
					}
				}

				merger.addSessionPath(loc);
				merger.setRecordFilter(loc, filter);
			}

			merger.mergeSessions();

			if(mergedDate != null) {
				final LocalDate dt = LocalDate.now();
				mergedSession.setDate(dt);
			}

			if(mergedMedia != null) {
				mergedSession.setMediaLocation(mergedMedia);
			}
			
			out.println("Saving session...");
			out.flush();

			// save
			final UUID writeLock = project.getSessionWriteLock(mergedSession);
			project.saveSession(mergedSession, writeLock);
			project.releaseSessionWriteLock(mergedSession, writeLock);

			out.println("Finished. New session has " + mergedSession.getRecordCount() + " records.");
			out.flush();
		} catch (IOException e) {
			out.println(e.getLocalizedMessage());
			LogUtil.severe(e);
			throw e;
		} finally {
			out.flush();
			out.close();
		}

	}

}
