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
package ca.phon.app.project.mergewizard;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ca.phon.app.log.BufferPanel;
import ca.phon.app.project.SessionMerger;
import ca.phon.project.Project;
import ca.phon.session.DateFormatter;
import ca.phon.session.Participant;
import ca.phon.session.RecordFilter;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SessionPath;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.wizard.WizardFrame;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.worker.PhonWorker;

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
		Logger.getLogger("ca.phon").addHandler( new Handler() {

			@Override
			public void publish(LogRecord record) {
				out.println(record.getMessage());
				out.flush();
			}

			@Override
			public void flush() {
			}

			@Override
			public void close() throws SecurityException {

			}

		});

		String corpus = step1.getMergedCorpusName();
		String session = step1.getMergedSessionName();

		List<SessionPath> sessions = step1.getSelectedSessions();
		Collections.sort(sessions);

		final Project project = getProject();
		// first make sure we have a corpus
		if(!project.getCorpora().contains(step1.getMergedCorpusName())) {

			LOGGER.info("Creating corpus '" + corpus + "'");
			try {
				project.addCorpus(corpus, "");
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				return;
			}

		}

		// check session name
		if(project.getCorpusSessions(corpus).contains(session)) {
			LOGGER.severe("A session with name '" + corpus + "." + session + "' already exists.");
			return;
		}

		// create the new session
		LOGGER.info("Creating session '" + corpus + "." + session + "'");
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

				LOGGER.info("Merging data from session '" + loc.getCorpus() + "." + loc.getSession() + "'");
				if(checkDate) {
					String tDate = pdf.format(t.getDate());
					if(mergedDate == null) {
						mergedDate = tDate;
					} else {
						if(!mergedDate.equals(tDate)) {
							LOGGER.warning("Session dates do not match, setting merged session date to today.");
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
							LOGGER.warning("Session media locations do not match, leaving media field blank.");
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

			LOGGER.info("Saving session...");

			// save
			final UUID writeLock = project.getSessionWriteLock(mergedSession);
			project.saveSession(mergedSession, writeLock);
			project.releaseSessionWriteLock(mergedSession, writeLock);

			LOGGER.info("Finished. New session has " + mergedSession.getRecordCount() + " records.");
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw e;
		} finally {
			out.close();
		}

	}

}
