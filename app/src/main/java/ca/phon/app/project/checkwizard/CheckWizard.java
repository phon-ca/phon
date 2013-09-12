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
package ca.phon.app.project.checkwizard;

import java.awt.BorderLayout;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ca.phon.alignment.Aligner;
import ca.phon.app.project.ProjectFrameExtension;
import ca.phon.app.project.checkwizard.CheckWizardStep1.Operation;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneAligner;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.ipa.parser.IPAParser;
import ca.phon.project.Project;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionLocation;
import ca.phon.session.Tier;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.ui.PhonLoggerConsole;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.wizard.WizardFrame;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonWorker;

/**
 * A wizard for checking/repairing IPA transcriptions
 * in a set of selected sessions.
 */
public class CheckWizard extends WizardFrame {
	
	private final static Logger LOGGER = Logger.getLogger(CheckWizard.class.getName());
	
	private PhonLoggerConsole console;
	
	private CheckWizardStep1 step1;
	
	private WizardStep opStep;

	/**
	 * Constructor
	 */
	public CheckWizard(Project project) {
		super("Phon : " + project.getName() + " : Check Transcriptions");
		
		setWindowName("Check Transcriptions");
		final ProjectFrameExtension projectExt = new ProjectFrameExtension(project);
		super.putExtension(ProjectFrameExtension.class, projectExt);
		
		init();
	}
	
	private void init() {
		super.btnFinish.setVisible(false);
		
		step1 = new CheckWizardStep1(getProject());
		addWizardStep(step1);
		
		opStep = createOpStep();
		
		step1.setNextStep(1);
		opStep.setPrevStep(0);
	}
	
	private WizardStep createOpStep() {
		JPanel checkPanel = new JPanel(new BorderLayout());
		
		DialogHeader importHeader = new DialogHeader("Check Transcriptions", "Performing selected operation");
		checkPanel.add(importHeader, BorderLayout.NORTH);
		
		JPanel consolePanel = new JPanel(new BorderLayout());
		
		console = new PhonLoggerConsole();
		consolePanel.add(console, BorderLayout.CENTER);
		
		checkPanel.add(consolePanel, BorderLayout.CENTER);
		
		return super.addWizardStep(checkPanel);
	}
	
	private Project getProject() {
		final ProjectFrameExtension pfe = getExtension(ProjectFrameExtension.class);
		if(pfe != null) {
			return pfe.getProject();
		}
		return null;
	}
	
	/**
	 * Check IPA action
	 */
	private class CheckIPA extends PhonTask {
		private String corpusName;
		private String sessionName;
		
		public CheckIPA(String c, String s) {
			corpusName = c;
			sessionName = s;
		}
		
		@Override
		public void performTask() {
			super.setStatus(TaskStatus.RUNNING);
			LOGGER.fine("Check IPA: " + corpusName + "." + sessionName);
			
			Session session = null;
			try {
				session = getProject().openSession(corpusName, sessionName);
			} catch (IOException e1) {
				LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
				return;
			}
			
			int progress = 0;
			for(int i = 0; i < session.getRecordCount(); i++) {
				int numErrors = 0;
				if(super.isShutdown()) {
					super.setStatus(TaskStatus.TERMINATED);
					return; // get out immediately
				}
				
				final Record record = session.getRecord(i);
				
				checkTier(record.getIPATarget());
				checkTier(record.getIPAActual());
			}
			
			super.setStatus(TaskStatus.FINISHED);
		}
		
		private void checkTier(Tier<IPATranscript> tier) {
			for(IPATranscript ipa:tier) {
				final String text = ipa.toString();
				
				// run text through parser manually
				try {
					IPATranscript.parseTranscript(text);
				} catch (ParseException pe) {
					LOGGER.log(Level.SEVERE, pe.getMessage(), pe);
				}
			}
		}
		
	}
	
	/**
	 * Reset syllabification action.
	 */
	private class ResetSyllabification extends PhonTask {

		private String corpusName;
		private String sessionName;
		private Syllabifier syllabifier;
		private boolean isResetAlignment = false;
		
		public ResetSyllabification(String c, String s, Syllabifier syllabifier,
				boolean resetAlignment) {
			corpusName = c;
			sessionName = s;
			this.syllabifier = syllabifier;
			this.isResetAlignment = resetAlignment;
		}
		
		@Override
		public void performTask() {
			super.setStatus(TaskStatus.RUNNING);
			LOGGER.info(
					"Reset Syllabification: " + corpusName + "." + sessionName);
			
			Session session = null;
			try {
				session = getProject().openSession(corpusName, sessionName);
			} catch (IOException e1) {
				LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
				return;
			}
			
//			glassPane.setProgressBarIntermediate(false);
//			glassPane.setProgressBarRange(1, transcript.getUtterances().size());
			
			int progress = 0;
			for(int i = 0; i < session.getRecordCount(); i++) {
				
				if(super.isShutdown()) {
					super.setStatus(TaskStatus.TERMINATED);
					return; // get out immediately
				}
				
				final Record record = session.getRecord(i);
				
				final Tier<IPATranscript> ipaTarget = record.getIPATarget();
				resetSyllabification(ipaTarget);
				final Tier<IPATranscript> ipaActual = record.getIPAActual();
				resetSyllabification(ipaActual);
			}
			
			final Project project = getProject();
			// save xml to project
			try {
				final UUID writeLock = project.getSessionWriteLock(session);
				
				if(writeLock == null) {
					LOGGER.warning(
							"Could not get write lock for: " + corpusName + "." + sessionName);
					return;
				}
				
				project.saveSession(session, writeLock);
				project.releaseSessionWriteLock(session, writeLock);
				
				super.setStatus(TaskStatus.FINISHED);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
				
				super.err = e;
				super.setStatus(TaskStatus.ERROR);
			}
		}
		
		private void resetSyllabification(Tier<IPATranscript> tier) {
			for(IPATranscript ipa:tier) {
				syllabifier.syllabify(ipa);
			}
		}
	}
	
	/**
	 * Reset alignment action.
	 */
	private class ResetAlignment extends PhonTask {

		private String corpusName;
		private String sessionName;
		
		public ResetAlignment(String c, String s) {
			corpusName = c;
			sessionName = s;
		}
		
		@Override
		public void performTask() {
			super.setStatus(TaskStatus.RUNNING);
//			glassPane.setProgressLabelText(
			LOGGER.info(
					"Reset Alignment: " + corpusName + "." + sessionName);
			
			final Project project = getProject();
			Session session = null;
			try {
				session = project.openSession(corpusName, sessionName);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
				return;
			}
			
			int progress = 0;
			for(int i = 0; i < session.getRecordCount(); i++) {
				
				if(super.isShutdown()) {
					super.setStatus(TaskStatus.TERMINATED);
					return; // get out immediately
				}
				final Record record = session.getRecord(i);				
				final Aligner<IPAElement> phoneAligner = new PhoneAligner();
				
				final Tier<IPATranscript> ipaTarget = record.getIPATarget();
				final Tier<IPATranscript> ipaActual = record.getIPAActual();
				
				if(ipaTarget.numberOfGroups() != ipaActual.numberOfGroups()) {
					LOGGER.warning("Alignment error in record " + (i+1));
					continue;
				}
			}
			
			// save xml to project
			try {
				final UUID writeLock = project.getSessionWriteLock(session);
				
				if(writeLock == null) {
					LOGGER.warning(
							"Could not get write lock for: " + corpusName + "." + sessionName);
					return;
				}
				
				project.saveSession(session, writeLock);
				project.releaseSessionWriteLock(session, writeLock);
				
				super.setStatus(TaskStatus.FINISHED);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
				super.err = e;
				super.setStatus(TaskStatus.ERROR);
			}
		}
		
	}

	private PhonWorker worker = null;
	@Override
	protected void next() {

		super.next();
		if(super.getCurrentStep() == opStep) {
			// build task list and start worker
			worker = PhonWorker.createWorker();
			worker.setFinishWhenQueueEmpty(true);
			worker.setName("Check transcriptions");
			console.addLogger(LOGGER);
			showBusyLabel(console);
			
			Runnable toRun = new Runnable() {
				@Override
				public void run() {
					Runnable turnOffBack = new Runnable() {
						@Override
						public void run() {
							btnBack.setEnabled(false);
//							btnCancel.setEnabled(false);
							showBusyLabel(console);
						}
					};
					
					SwingUtilities.invokeLater(turnOffBack);
				}
			};
			worker.invokeLater(toRun);
			
			for(SessionLocation sessionLocation:step1.getSelectedSessions()) {
				PhonTask t = createTask(sessionLocation);
				worker.invokeLater(t);
			}
			
			Runnable atEnd = new Runnable() {
				@Override
				public void run() {
					Runnable turnOffBack = new Runnable() {
						@Override
						public void run() {
							console.removeLogger(LOGGER);
							btnBack.setEnabled(true);
							btnCancel.setEnabled(true);
							stopBusyLabel();
							worker = null;
						}
					};
					
					SwingUtilities.invokeLater(turnOffBack);
				}
			};
			
			worker.setFinalTask(atEnd);
			
			worker.start();
		}
	}
	
	private PhonTask createTask(SessionLocation location) {
		PhonTask retVal = null;
		
		Operation op = step1.getOperation();
		if(op == Operation.CHECK_IPA) {
			return new CheckIPA(location.getCorpus(), location.getSession());
		} else if(op == Operation.RESET_SYLLABIFICATION) {
			return new ResetSyllabification(location.getCorpus(), location.getSession(),
					step1.getSyllabifier(), step1.isResetAlignment());
		} else if(op == Operation.RESET_ALIGNMENT) {
			return new ResetAlignment(location.getCorpus(), location.getSession());
		}
		
		return retVal;
	}

	@Override
	protected void finish() {
		if(worker != null) {
			worker.shutdown();
		}
		super.finish();
	}

	@Override
	protected void cancel() {
		if(worker != null) {
			worker.shutdown();
		}
		super.cancel();
	}
	
}
