package ca.phon.app.project;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.session.SessionSelector;
import ca.phon.project.Project;
import ca.phon.session.Participant;
import ca.phon.session.ParticipantRole;
import ca.phon.session.Participants;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;
import ca.phon.session.Sex;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.wizard.WizardFrame;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonWorker;
import ca.phon.worker.PhonTask.TaskStatus;

/**
 * Wizard for stripping participant info.
 *
 */
public class AnonymizeParticipantInfoWizard extends WizardFrame {
	
	private static final Logger LOGGER = Logger
			.getLogger(AnonymizeParticipantInfoWizard.class.getName());
	
	private static final long serialVersionUID = -1433616585660247292L;
	
	private JCheckBox assignIDBox;
	
	private JCheckBox stripNameBox;
	
	private JCheckBox stripBirthdayBox;
	
	private JCheckBox stripAgeBox;
	
	private JCheckBox stripLangaugeBox;
	
	private JCheckBox stripEducationBox;
	
	private JCheckBox stripSESBox;
	
	private JCheckBox stripSexBox;
	
	private JCheckBox stripGroupBox;
	
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
		
		final JPanel optsPanel = new JPanel(new GridLayout(0, 3));
		assignIDBox = new JCheckBox("Assign ID from role");
		assignIDBox.setSelected(true);
		optsPanel.add(assignIDBox);
		
		stripNameBox = new JCheckBox("Name");
		stripNameBox.setSelected(true);
		optsPanel.add(stripNameBox);
		
		stripSexBox = new JCheckBox("Sex");
		stripSexBox.setSelected(true);
		optsPanel.add(stripSexBox);
		
		stripBirthdayBox = new JCheckBox("Birthday");
		stripBirthdayBox.setSelected(true);
		optsPanel.add(stripBirthdayBox);
		
		stripAgeBox = new JCheckBox("Age");
		stripAgeBox.setSelected(true);
		optsPanel.add(stripAgeBox);
		
		stripLangaugeBox = new JCheckBox("Language");
		stripLangaugeBox.setSelected(true);
		optsPanel.add(stripLangaugeBox);
		
		stripEducationBox = new JCheckBox("Education");
		stripEducationBox.setSelected(true);
		optsPanel.add(stripEducationBox);
		
		stripGroupBox = new JCheckBox("Group");
		stripGroupBox.setSelected(true);
		optsPanel.add(stripGroupBox);
		
		stripSESBox = new JCheckBox("SES");
		stripSESBox.setSelected(true);
		optsPanel.add(stripSESBox);
		
		optsPanel.setBorder(BorderFactory.createTitledBorder("Select information to strip"));
		
		step1.add(header, BorderLayout.NORTH);
		
		final JPanel contents = new JPanel(new BorderLayout());
		contents.add(optsPanel, BorderLayout.NORTH);
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
					
					for(Participant p:parts) {
						if(assignIDBox.isSelected()) {
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
						
						if(stripNameBox.isSelected()) {
							p.setName(null);
						}
						if(stripBirthdayBox.isSelected()) {
							p.setBirthDate(null);
						}
						if(stripAgeBox.isSelected()) {
							p.setAge(null);
						}
						if(stripLangaugeBox.isSelected()) {
							p.setLanguage(null);
						}
						if(stripEducationBox.isSelected()) {
							p.setEducation(null);
						}
						if(stripSESBox.isSelected()) {
							p.setSES(null);
						}
						if(stripSexBox.isSelected()) {
							p.setSex(Sex.UNSPECIFIED);
						}
						if(stripGroupBox.isSelected()) {
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
