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
package ca.phon.app.project.checkwizard;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.LogBuffer;
import ca.phon.app.project.checkwizard.CheckWizardStep1.Operation;
import ca.phon.extensions.UnvalidatedValue;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneAligner;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.project.Project;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;
import ca.phon.session.Tier;
import ca.phon.syllabifier.Syllabifier;
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
	
	private BufferPanel bufferPanel;
	
	private CheckWizardStep1 step1;
	
	private WizardStep opStep;

	/**
	 * Constructor
	 */
	public CheckWizard(Project project) {
		super("Phon : " + project.getName() + " : Check Transcriptions");
		
		setWindowName("Check Transcriptions");
		super.putExtension(Project.class, project);
		
		btnCancel.setText("Close");
		
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
		
		bufferPanel = new BufferPanel("Check Transcripts");
		consolePanel.add(bufferPanel, BorderLayout.CENTER);
		
		checkPanel.add(consolePanel, BorderLayout.CENTER);
		
		return super.addWizardStep(checkPanel);
	}
	
	private Project getProject() {
		return getExtension(Project.class);
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
			try {
				final OutputStreamWriter out = new OutputStreamWriter(bufferPanel.getLogBuffer().getStdOutStream(), "UTF-8");
				Session session = null;
				try {
					session = getProject().openSession(corpusName, sessionName);
				} catch (IOException e1) {
					LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
					return;
				}
				
				for(int i = 0; i < session.getRecordCount(); i++) {
					if(super.isShutdown()) {
						super.setStatus(TaskStatus.TERMINATED);
						return; // get out immediately
					}
					
					final Record record = session.getRecord(i);
					
					checkTier(i, record.getIPATarget(), out);
					checkTier(i, record.getIPAActual(), out);
				}
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
			
			super.setStatus(TaskStatus.FINISHED);
		}
		
		private void checkTier(int record, Tier<IPATranscript> tier, OutputStreamWriter out) throws IOException {
			for(int gIdx = 0; gIdx < tier.numberOfGroups(); gIdx++) {
				final IPATranscript ipa = tier.getGroup(gIdx);
				// check for 'UnvalidatedValue's
				final UnvalidatedValue uv = ipa.getExtension(UnvalidatedValue.class);
				if(uv != null) {
					out.write("\"" + corpusName + "." + sessionName + "\",");
					out.write("\"" + (record+1) + "\",");
					out.write("\"" + tier.getName() + "\",");
					out.write("\"" + (gIdx+1) + "\",");
					out.write("\"" + uv.getValue() + "\",");
					out.write("\"" + uv.getParseError().getLocalizedMessage() + "\"\n");
				}
			}
			out.flush();
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
			final StringBuilder sb = new StringBuilder();
			final PrintWriter out = new PrintWriter(bufferPanel.getLogBuffer().getStdOutStream());
			
			Session session = null;
			try {
				session = getProject().openSession(corpusName, sessionName);
			} catch (IOException e1) {
				LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
				out.println(e1.getLocalizedMessage());
				return;
			}
			
			final PhoneAligner phoneAligner = new PhoneAligner();
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
				
				if(isResetAlignment) {
					if(ipaTarget.numberOfGroups() != ipaActual.numberOfGroups()) {
						out.println("Alignment error in record " + (i+1));
						continue;
					}
					
					final Tier<PhoneMap> alignmentTier = record.getPhoneAlignment();
					for(int j = 0; j < ipaTarget.numberOfGroups(); j++) {
						final IPATranscript target = ipaTarget.getGroup(j);
						final IPATranscript actual = ipaActual.getGroup(j);
						
						final PhoneMap pm = phoneAligner.calculatePhoneMap(target, actual);
						alignmentTier.setGroup(j, pm);
					}
				}
			}
			
			final Project project = getProject();
			// save xml to project
			try {
				final UUID writeLock = project.getSessionWriteLock(session);
				
				if(writeLock == null) {
					out.println(
							"Could not get write lock for: " + corpusName + "." + sessionName);
					return;
				}
				
				try {
					project.saveSession(session, writeLock);
				} catch (IOException e) {
					out.println(e.getLocalizedMessage());
				} finally {
					project.releaseSessionWriteLock(session, writeLock);
				}
				
				super.setStatus(TaskStatus.FINISHED);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
				
				super.err = e;
				super.setStatus(TaskStatus.ERROR);
			}
		}
		
		private void resetSyllabification(Tier<IPATranscript> tier) {
			for(IPATranscript ipa:tier) {
				ipa.resetSyllabification();
				syllabifier.syllabify(ipa.toList());
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
			
			final StringBuilder sb = new StringBuilder();
			final PrintWriter out = new PrintWriter(bufferPanel.getLogBuffer().getStdOutStream());
			
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
				final PhoneAligner phoneAligner = new PhoneAligner();
				
				final Tier<IPATranscript> ipaTarget = record.getIPATarget();
				final Tier<IPATranscript> ipaActual = record.getIPAActual();
				
				if(ipaTarget.numberOfGroups() != ipaActual.numberOfGroups()) {
					out.println("Alignment error in record " + (i+1));
					continue;
				}
				
				final Tier<PhoneMap> alignmentTier = record.getPhoneAlignment();
				for(int j = 0; j < ipaTarget.numberOfGroups(); j++) {
					final IPATranscript target = ipaTarget.getGroup(j);
					final IPATranscript actual = ipaActual.getGroup(j);
					
					final PhoneMap pm = phoneAligner.calculatePhoneMap(target, actual);
					alignmentTier.setGroup(j, pm);
				}
			}
			
			// save xml to project
			try {
				final UUID writeLock = project.getSessionWriteLock(session);
				
				if(writeLock == null) {
					out.println(
							"Could not get write lock for: " + corpusName + "." + sessionName);
					return;
				}
				
				try {
					project.saveSession(session, writeLock);
				} catch (IOException e) {
					out.println(e.getLocalizedMessage());
				} finally {
					project.releaseSessionWriteLock(session, writeLock);
				}
				
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
			
			if(!bufferPanel.isShowingBuffer()) {
				bufferPanel.clear();
			}
			try {
				final OutputStreamWriter out = new OutputStreamWriter(bufferPanel.getLogBuffer().getStdOutStream(), "UTF-8");
				final StringBuilder sb = new StringBuilder();
				sb.append('\"').append("Session").append('\"').append(',');
				sb.append('\"').append("Record #").append('\"').append(',');
				sb.append('\"').append("Tier").append('\"').append(',');
				sb.append('\"').append("Group").append('\"').append(',');
				sb.append('\"').append("Value").append('\"').append(',');
				sb.append('\"').append("Error").append('\"').append('\n');
				out.write(sb.toString());
				out.flush();
				
				Runnable toRun = new Runnable() {
					@Override
					public void run() {
						Runnable turnOffBack = new Runnable() {
							@Override
							public void run() {
								btnBack.setEnabled(false);
							}
						};
						SwingUtilities.invokeLater(turnOffBack);
						try {
						out.flush();
						out.write(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_BUSY);
						out.flush();
						} catch (IOException e) {
							LOGGER.log(Level.SEVERE,
									e.getLocalizedMessage(), e);
						}
						
					}
				};
				worker.invokeLater(toRun);
				
				for(SessionPath sessionLocation:step1.getSelectedSessions()) {
					PhonTask t = createTask(sessionLocation);
					worker.invokeLater(t);
				}
				
				Runnable atEnd = new Runnable() {
					@Override
					public void run() {
						Runnable turnOffBack = new Runnable() {
							@Override
							public void run() {
								btnBack.setEnabled(true);
								btnCancel.setEnabled(true);
								
								worker = null;
							}
						};
						SwingUtilities.invokeLater(turnOffBack);
						try {
							out.flush();
							out.write(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.STOP_BUSY);
							out.flush();
							out.write(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_TABLE_CODE);
							out.flush();
							out.write(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.PACK_TABLE_COLUMNS);
							out.flush();
						} catch(IOException e) {
							LOGGER.log(Level.SEVERE,
									e.getLocalizedMessage(), e);
						}
					}
				};
				
				worker.setFinalTask(atEnd);
				
				worker.start();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}
	
	private PhonTask createTask(SessionPath location) {
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
