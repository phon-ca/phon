package ca.phon.app.project;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.joda.time.Period;

import ca.phon.app.session.SessionSelector;
import ca.phon.project.Project;
import ca.phon.session.Participant;
import ca.phon.session.Participants;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;
import ca.phon.session.Sex;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.participant.AnonymizeParticipantOptionsPanel;
import ca.phon.ui.wizard.WizardFrame;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTask.TaskStatus;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonWorker;

/**
 * Wizard for stripping participant info.
 *
 */
public class AnonymizeParticipantInfoWizard extends WizardFrame {
	
	private static final Logger LOGGER = Logger
			.getLogger(AnonymizeParticipantInfoWizard.class.getName());
	
	private static final long serialVersionUID = -1433616585660247292L;
	
	private AnonymizeParticipantOptionsPanel optionsPanel;
	
	// UI
	private SessionSelector sessionSelector;

	public AnonymizeParticipantInfoWizard(Project project) {
		super("Strip Participant Information");
		putExtension(Project.class, project);
		
		init();
	}
	
	private void init() {
		super.btnBack.setVisible(false);
		super.btnNext.setVisible(false);
		
		super.btnFinish.setText("Anonymize");
		super.btnFinish.setIcon(null);
		super.btnCancel.setText("Close");
		
		final WizardStep step1 = new WizardStep();
		step1.setLayout(new BorderLayout());
		
		final DialogHeader header = new DialogHeader("Anonymize Participant Information", 
				"Remove participant information for selected sessions.");
		
		final Project project = getExtension(Project.class);
		sessionSelector = new SessionSelector(project);
		final JScrollPane scroller = new JScrollPane(sessionSelector);
		scroller.setBorder(BorderFactory.createTitledBorder("Select sessions"));
		
		optionsPanel = new AnonymizeParticipantOptionsPanel();
		optionsPanel.setBorder(BorderFactory.createTitledBorder("Select information to strip"));
		
		step1.add(header, BorderLayout.NORTH);
		
		final JPanel contents = new JPanel(new BorderLayout());
		contents.add(optionsPanel, BorderLayout.NORTH);
		contents.add(scroller, BorderLayout.CENTER);
		step1.add(contents, BorderLayout.CENTER);
		
		addWizardStep(step1);
	}
	
	@Override
	public void finish() {
		final PhonTask stripTask = new StripTask();
		stripTask.addTaskListener(new PhonTaskListener() {
			
			@Override
			public void statusChanged(PhonTask task, TaskStatus oldStatus,
					TaskStatus newStatus) {
				if(newStatus == TaskStatus.RUNNING) {
					showBusyLabel(sessionSelector);
				} else {
					stopBusyLabel();
				}
				btnFinish.setEnabled(newStatus != TaskStatus.RUNNING);
			}
			
			@Override
			public void propertyChanged(PhonTask task, String property,
					Object oldValue, Object newValue) {
				// TODO Auto-generated method stub
				
			}
		});
		PhonWorker.getInstance().invokeLater(stripTask);
	}

	private class StripTask extends PhonTask {
		
		@Override
		public void performTask() {
			super.setStatus(TaskStatus.RUNNING);
			
			final Project project = getExtension(Project.class);
			for(SessionPath sp:sessionSelector.getSelectedSessions()) {
				try {
					final Session session = project.openSession(sp.getCorpus(), sp.getSession());
					final Participants parts = session.getParticipants();
					
					// XXX we need to ensure all records are loaded
					// if changing id
					if(optionsPanel.isAssignId()) {
						for(Record r:session.getRecords()) {
							r.getSpeaker();
						}
					}
					
					for(Participant p:parts) {
						if(optionsPanel.isAssignId()) {
							
							
							String id = p.getRole().getId();
							
							int idx = 0;
							// look at other participants, see if we need to modify id
							for(Participant otherP:parts) {
								if(otherP == p) continue;
								if(otherP.getId().equals(id)) {
									id = p.getRole().getId().substring(0, 2) + (++idx);
								}
							}
							p.setId(id);
						}
						
						if(optionsPanel.isAnonName()) {
							p.setName(null);
						}
						
						final Period calculatedAge = 
								(session.getDate() != null && p.getBirthDate() != null ? p.getAge(session.getDate()) : null);
						if(optionsPanel.isAnonBday()) {
							p.setBirthDate(null);
						}
						if(optionsPanel.isAnonAge()) {
							p.setAge(null);
						} else {
							if(calculatedAge != null && p.getAge(null) == null) {
								p.setAge(calculatedAge);
							}
						}
						if(optionsPanel.isAnonLang()) {
							p.setLanguage(null);
						}
						if(optionsPanel.isAnonEdu()) {
							p.setEducation(null);
						}
						if(optionsPanel.isAnonSes()) {
							p.setSES(null);
						}
						if(optionsPanel.isAnonSex()) {
							p.setSex(Sex.UNSPECIFIED);
						}
						if(optionsPanel.isAnonGroup()) {
							p.setGroup(null);
						}
					}
					
					final UUID writeLock = project.getSessionWriteLock(session);
					project.saveSession(session, writeLock);
					project.releaseSessionWriteLock(session, writeLock);
				} catch (IOException e) {
					LOGGER
							.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
			
			super.setStatus(TaskStatus.FINISHED);
		}
		
	}
	
}
