package ca.phon.app.project;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXBusyLabel;

import ca.phon.app.session.ParticipantSelector;
import ca.phon.app.session.SessionSelector;
import ca.phon.project.Project;
import ca.phon.session.Participant;
import ca.phon.session.SessionPath;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxState;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTree;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeModel;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeModelEvent;
import ca.phon.worker.PhonWorker;

/**
 * UI for viewing participants in a project.  This view has has two parts,
 * a {@link TristateCheckBoxTree} for selecting sessions and another for
 * listing {@link Participant}s indentified in checked sessions.  A basic
 * history, including # of records and age, is provided for each
 * session the {@link Participant} is found.
 * 
 */
public class ParticipantsPanel extends JPanel {
	
	private static final long serialVersionUID = 427615208570981191L;

	private SessionSelector sessionSelector = new SessionSelector();
	private ParticipantSelector participantSelector = new ParticipantSelector();
	private JXBusyLabel participantBusyLabel = new JXBusyLabel(new Dimension(16, 16));
	
	private Project project;
	
	public ParticipantsPanel(Project project) {
		super();
		
		this.project = project;
		
		init();
	}
	
	private void init() {
		setLayout(new GridBagLayout());
		
		sessionSelector = new SessionSelector();
		
		final TitledPanel sessionPanel = new TitledPanel("Sessions");
		sessionPanel.getContentContainer().setLayout(new BorderLayout());
		final JScrollPane sessionScroller = new JScrollPane(sessionSelector);
		sessionSelector.getModel().addTreeModelListener(treeModelListener);
		sessionPanel.getContentContainer().add(sessionScroller, BorderLayout.CENTER);
		
		final TitledPanel participantPanel = new TitledPanel("Participants");
		participantSelector = new ParticipantSelector();
		participantSelector.setRootVisible(false);
		final JScrollPane participantScroller = new JScrollPane(participantSelector);
		participantPanel.getContentContainer().setLayout(new BorderLayout());
		participantPanel.getContentContainer().add(participantScroller, BorderLayout.CENTER);
		
		participantBusyLabel = new JXBusyLabel(new Dimension(16, 16));
		participantPanel.setLeftDecoration(participantBusyLabel);
		
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridheight = 2;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(2, 2, 2, 2);
		add(sessionPanel, gbc);
		
		++gbc.gridx;
		add(participantPanel, gbc);
	}
	
	private void updateParticipants() {
		final Runnable onBg = () -> {
			SwingUtilities.invokeLater(() -> participantBusyLabel.setBusy(true));
			
			final Collection<Participant> participantHistory =
					project.getParticipants(sessionSelector.getSelectedSessions());
			
			final TristateCheckBoxTreeModel model = ParticipantSelector.createModel(participantHistory);
			SwingUtilities.invokeLater( () -> { 
				participantSelector.setModel(model);
				participantSelector.setCheckingStateForPath(new TreePath(participantSelector.getRoot()), TristateCheckBoxState.CHECKED);
				participantBusyLabel.setBusy(false);
				
				participantSelector.expandAll(new TreePath(participantSelector.getRoot()));
			});
		};
		PhonWorker.getInstance().invokeLater(onBg);
	}
	
	public void setProject(Project project) {
		this.project = project;
		sessionSelector.setProject(project);
		sessionSelector.getModel().addTreeModelListener(treeModelListener);
		sessionSelector.revalidate();
	}
	
	public Project getProject() {
		return this.project;
	}
	
	public SessionSelector getSessionSelector() {
		return this.sessionSelector;
	}
	
	public ParticipantSelector getParticipantSelector() {
		return this.participantSelector;
	}
	
	public Collection<SessionPath> getCheckedSessions() {
		return sessionSelector.getSelectedSessions();
	}
	
	public Collection<Participant> getCheckedParticipants() {
		return participantSelector.getSelectedParticpants();
	}

	private final TreeModelListener treeModelListener = new TreeModelListener() {
		
		@Override
		public void treeStructureChanged(TreeModelEvent e) {
		}
		
		@Override
		public void treeNodesRemoved(TreeModelEvent e) {
		}
		
		@Override
		public void treeNodesInserted(TreeModelEvent e) {
		}
		
		@Override
		public void treeNodesChanged(TreeModelEvent e) {
			if(e instanceof TristateCheckBoxTreeModelEvent) {
				updateParticipants();
			}
		}
		
	};
}
