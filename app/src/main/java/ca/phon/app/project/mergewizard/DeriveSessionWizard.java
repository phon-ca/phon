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
package ca.phon.app.project.mergewizard;

import java.awt.BorderLayout;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ca.phon.application.PhonWorker;
import ca.phon.application.project.IPhonProject;
import ca.phon.application.transcript.ITranscript;
import ca.phon.application.transcript.SessionLocation;
import ca.phon.application.transcript.UtteranceFilter;
import ca.phon.gui.DialogHeader;
import ca.phon.gui.components.PhonLoggerConsole;
import ca.phon.modules.project.SessionMerger;
import ca.phon.system.logger.PhonLogger;
import ca.phon.ui.wizard.WizardFrame;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.PhonDateFormat;
import ca.phon.util.StringUtils;

/**
 * Merge one or more session into a new session.
 *
 */
public class DeriveSessionWizard extends WizardFrame {
	
	/*
	 * Steps
	 */
	private MergeSessionStep1 step1;
	
	private MergeSessionStep2 step2;
	
	private WizardStep mergeStep;
	
	private WizardStep importStep;
	
	private PhonLoggerConsole console;

	public DeriveSessionWizard(IPhonProject project) {
		super("Phon : " + project.getProjectName() + " : Derive Session");
		super.setProject(project);
		setWindowName("Derive Session");
		
		init();
	}
	
	private void init() {
		btnCancel.setText("Close");
		btnFinish.setVisible(false);
		
		step1 = new MergeSessionStep1(super.getProject());
		step1.setNextStep(0);
		addWizardStep(step1);
		
//		addWizardStep(new JPanel());
	}

	private WizardStep createMergeStep() {
		JPanel importPanel = new JPanel(new BorderLayout());
		
		DialogHeader importHeader = new DialogHeader("Derive Session", "Merge data from one or more sessions into a new session.");
		importPanel.add(importHeader, BorderLayout.NORTH);
		
		JPanel consolePanel = new JPanel(new BorderLayout());
		
		console = new PhonLoggerConsole();
		consolePanel.add(console, BorderLayout.CENTER);
		
		importPanel.add(consolePanel, BorderLayout.CENTER);
		
		return super.addWizardStep(importPanel);
	}
	
	@Override
	protected void next() {
		if(super.getCurrentStep() == step1) {
			// setup step2
			if(step2 == null) {
				step2 = new MergeSessionStep2(super.getProject(), step1.getSelectedSessions());
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
		
		console.addReportThread(worker);
		
		Runnable toRun = new Runnable() {
			@Override
			public void run() {
				Runnable turnOffBack = new Runnable() {
					@Override
					public void run() {
						console.startLogging();
						btnBack.setEnabled(false);
						btnCancel.setEnabled(false);
						showBusyLabel(console);
					}
				};
				Runnable turnOnBack = new Runnable() {
					@Override
					public void run() {
						console.stopLogging();
						btnBack.setEnabled(true);
						btnCancel.setEnabled(true);
						stopBusyLabel();
					}
				};
				SwingUtilities.invokeLater(turnOffBack);
				
				doMerge();
				
				SwingUtilities.invokeLater(turnOnBack);
			}
		};
		worker.invokeLater(toRun);
		
		worker.start();
	}
	
	/**
	 * Perform the merge.
	 */
	private void doMerge() {
		
		long currentTime = System.currentTimeMillis();
		
		String corpus = step1.getMergedCorpusName();
		String session = step1.getMergedSessionName();
		
//		Collator collator = CollatorFactory.defaultCollator();
		List<SessionLocation> sessions = step1.getSelectedSessions();
		Collections.sort(sessions);
		
		// first make sure we have a corpus
		if(!project.getCorpora().contains(step1.getMergedCorpusName())) {
			
			PhonLogger.info("Creating corpus '" + corpus + "'");
			try {
				project.newCorpus(corpus, "");
			} catch (IOException e) {
				PhonLogger.severe(e.toString());
				return;
			}
			
		}
		
		// check session name
		if(project.getCorpusTranscripts(corpus).contains(session)) {
			PhonLogger.severe("A session with name '" + corpus + "." + session + "' already exists.");
			return;
		}
		
		// create the new session
		PhonLogger.info("Creating session '" + corpus + "." + session + "'");
		try {
			ITranscript mergedSession = project.newTranscript(corpus, session);
			
			PhonDateFormat pdf = new PhonDateFormat(PhonDateFormat.YEAR_LONG);
			
			// merge sessions
			// keep track of session/media to see if we can sucessfully copy the data over
			String mergedDate = null;
			boolean checkDate = true;
			String mergedMedia = null;
			boolean checkMedia = true;
			for(SessionLocation loc:sessions) {
				ITranscript t = step2.getSessionAtLocation(loc);
				UtteranceFilter filter = step2.getFilterForLocation(loc);
				
				PhonLogger.info("Merging data from session '" + loc.getCorpus() + "." + loc.getSession() + "'");
				if(checkDate) {
					String tDate = pdf.format(t.getDate());
					if(mergedDate == null) {
						mergedDate = tDate;
					} else {
						if(!mergedDate.equals(tDate)) {
							PhonLogger.warning("Session dates do not match, setting merged session date to today.");
							mergedDate = pdf.format(Calendar.getInstance());
							checkDate = false;
						}
					}
				}
				
				if(checkMedia) {
					if(mergedMedia == null) {
						mergedMedia = t.getMediaLocation();
					} else {
						if(!mergedMedia.equals(t.getMediaLocation())) {
							PhonLogger.warning("Session media locations do not match, leaving media field blank.");
							mergedMedia = "";
							checkMedia = false;
						}
					}
				}
				
				// merge this session info
				SessionMerger.mergeSession(mergedSession, t, filter);
			}
			
			// setup date and media
			if(mergedDate != null) {
				try {
					Calendar sessionDate = (Calendar)pdf.parseObject(mergedDate);
					mergedSession.setDate(sessionDate);
				} catch (ParseException e) {
					PhonLogger.warning(e.toString());
				}
			}
			
			if(mergedMedia != null) {
				mergedSession.setMediaLocation(mergedMedia);
			}
			
			// save session
			int writeLock = project.getTranscriptWriteLock(corpus, session);
			project.saveTranscript(mergedSession, writeLock);
			project.releaseTranscriptWriteLock(corpus, session, writeLock);
		} catch (IOException e) {
			PhonLogger.severe(e.toString());
			return;
		}
		
		long endTime = System.currentTimeMillis();
		PhonLogger.info("Task finished.  Total time " + StringUtils.msToWrittenString(endTime-currentTime));
		
	}
	
}
