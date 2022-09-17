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
package ca.phon.app.project.checkwizard;

import ca.phon.app.log.*;
import ca.phon.app.project.checkwizard.CheckWizardStep1.Operation;
import ca.phon.extensions.UnvalidatedValue;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.*;
import ca.phon.project.Project;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.wizard.*;
import ca.phon.worker.*;
import org.jdesktop.swingx.JXBusyLabel;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.UUID;

/**
 * A wizard for checking/repairing IPA transcriptions
 * in a set of selected sessions.
 */
public class CheckWizard extends BreadcrumbWizardFrame {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(CheckWizard.class.getName());
	
	private JXBusyLabel busyLabel;
	
	private MultiBufferPanel multiBufferPanel;
	
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
		step1 = new CheckWizardStep1(getProject());
		step1.setTitle("Select Sessions");
		addWizardStep(step1);
		
		opStep = createOpStep();
		addWizardStep(opStep);
		
		step1.setNextStep(1);
		opStep.setPrevStep(0);
		
		breadCrumbViewer.setVisible(true);
	}
	
	private WizardStep createOpStep() {
		JPanel checkPanel = new JPanel(new BorderLayout());

		multiBufferPanel = new MultiBufferPanel();
		checkPanel.add(multiBufferPanel, BorderLayout.CENTER);
		
		WizardStep retVal = new WizardStep();
		retVal.setLayout(new BorderLayout());
		
		busyLabel = new JXBusyLabel(new Dimension(16, 16));
		
		final TitledPanel titledPanel = new TitledPanel("Errors", checkPanel);
		titledPanel.setLeftDecoration(busyLabel);
		
		retVal.add(titledPanel, BorderLayout.CENTER);
		
		retVal.setTitle("Check Transcriptions");
		
		return retVal;
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
		private BufferPanel bufferPanel;
		
		public CheckIPA(String c, String s, BufferPanel bufferPanel) {
			corpusName = c;
			sessionName = s;
			this.bufferPanel = bufferPanel;
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
					LOGGER.error( e1.getMessage(), e1);
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
				LOGGER.error( e.getLocalizedMessage(), e);
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
		private BufferPanel bufferPanel;
		
		public ResetSyllabification(String c, String s, Syllabifier syllabifier,
				boolean resetAlignment, BufferPanel bufferPanel) {
			corpusName = c;
			sessionName = s;
			this.syllabifier = syllabifier;
			this.isResetAlignment = resetAlignment;
			this.bufferPanel = bufferPanel;
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
				LOGGER.error( e1.getMessage(), e1);
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
				LOGGER.error( e.getMessage(), e);
				
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
		private BufferPanel bufferPanel;
		
		public ResetAlignment(String c, String s, BufferPanel bufferPanel) {
			corpusName = c;
			sessionName = s;
			this.bufferPanel = bufferPanel;
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
				LOGGER.error( e.getMessage(), e);
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
				LOGGER.error( e.getMessage(), e);
				super.err = e;
				super.setStatus(TaskStatus.ERROR);
			}
		}
		
	}

	private void setupTableHeader(BufferPanel bufferPanel) throws IOException {
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
	}
	
	private String getOperationName() {
		String retVal = "";
		Operation op = step1.getOperation();
		if(op == Operation.CHECK_IPA) {
			retVal = "Check Transcriptions";
		} else if(op == Operation.RESET_SYLLABIFICATION) {
			retVal = "Reset Syllabification";
		} else if(op == Operation.RESET_ALIGNMENT) {
			retVal = "Reset Alignment";
		}
		return retVal;
	}
		
	private PhonWorker worker = null;
	@Override
	protected void next() {
		if(getCurrentStep() == step1) {
			opStep.setTitle(getOperationName());
		}
		super.next();
		if(super.getCurrentStep() == opStep) {
			// build task list and start worker
			worker = PhonWorker.createWorker();
			worker.setFinishWhenQueueEmpty(true);
			worker.setName("Check transcriptions");
			
			final BufferPanel bufferPanel = multiBufferPanel.createBuffer(getOperationName());
			try {
				setupTableHeader(bufferPanel);
			} catch (IOException e) {
				LOGGER.warn( e.getLocalizedMessage(), e);
			}
			
			busyLabel.setBusy(true);
			for(SessionPath sessionLocation:step1.getSelectedSessions()) {
				PhonTask t = createTask(sessionLocation, bufferPanel);
				worker.invokeLater(t);
			}
			worker.setFinalTask( () -> {
				SwingUtilities.invokeLater( () -> {
					busyLabel.setBusy(false);
					bufferPanel.showTable();
					
					showMessageDialog(bufferPanel.getName(), "Operation complete", new String[] {"Ok"} );
				});
			});
			
			worker.start();
		}
	}
	
	private PhonTask createTask(SessionPath location, BufferPanel bufferPanel) {
		PhonTask retVal = null;
		
		Operation op = step1.getOperation();
		if(op == Operation.CHECK_IPA) {
			return new CheckIPA(location.getCorpus(), location.getSession(), bufferPanel);
		} else if(op == Operation.RESET_SYLLABIFICATION) {
			return new ResetSyllabification(location.getCorpus(), location.getSession(),
					step1.getSyllabifier(), step1.isResetAlignment(), bufferPanel);
		} else if(op == Operation.RESET_ALIGNMENT) {
			return new ResetAlignment(location.getCorpus(), location.getSession(), bufferPanel);
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
