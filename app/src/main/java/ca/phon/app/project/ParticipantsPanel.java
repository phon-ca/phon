/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXBusyLabel;

import ca.phon.app.session.ParticipantSelector;
import ca.phon.app.session.SessionSelector;
import ca.phon.project.ParticipantCache;
import ca.phon.project.Project;
import ca.phon.session.Participant;
import ca.phon.session.SessionPath;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxState;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTree;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeModel;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeModelEvent;

/**
 * UI for viewing participants in a project.  This view has has two parts,
 * a {@link TristateCheckBoxTree} for selecting sessions and another for
 * listing {@link Participant}s identified in checked sessions.  A basic
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

	private ParticipantCache cache;

	public ParticipantsPanel() {
		this(null);
	}

	public ParticipantsPanel(Project project) {
		super();

		setProject(project);

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
		final JScrollPane participantScroller = new JScrollPane(participantSelector);
		participantPanel.getContentContainer().setLayout(new BorderLayout());
		participantPanel.getContentContainer().add(participantScroller, BorderLayout.CENTER);

		participantBusyLabel = new JXBusyLabel(new Dimension(16, 16));
		participantBusyLabel.getBusyPainter().setHighlightColor(Color.white);
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
		
		if(this.project != null) {
			this.cache = project.getExtension(ParticipantCache.class);
			sessionSelector.setProject(project);
			sessionSelector.getModel().addTreeModelListener(treeModelListener);
			sessionSelector.revalidate();
		}
	}

	public void setProject(Project project) {
		this.project = project;
		if(this.project != null) {
			this.cache = project.getExtension(ParticipantCache.class);
			sessionSelector.setProject(project);
			sessionSelector.getModel().addTreeModelListener(treeModelListener);
			sessionSelector.revalidate();
		}
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
				participantBusyLabel.setBusy(true);

				ParticipantWorker worker = new ParticipantWorker(sessionSelector.getSelectedSessions());
				worker.execute();
			}
		}

	};

	private class ParticipantWorker extends SwingWorker<List<SessionPath>, SessionPath> {

		private List<SessionPath> sessionPaths;

		private List<SessionPath> processedPaths = new ArrayList<>();

		public ParticipantWorker(Collection<SessionPath> paths) {
			this.sessionPaths = new ArrayList<>(paths);
		}

		@Override
		protected List<SessionPath> doInBackground() throws Exception {
			for(SessionPath path:sessionPaths) {
				cache.loadSession(path);
				publish(path);
			}

			return sessionPaths;
		}

		@Override
		protected void process(List<SessionPath> chunks) {
			processedPaths.addAll(chunks);
		}

		@Override
		protected void done() {
			participantBusyLabel.setBusy(false);

			List<Participant> participants = new ArrayList<>(cache.getParticipants(processedPaths));
			participants.sort( (p1, p2) -> p1.toString().compareTo(p2.toString()) );
			final TristateCheckBoxTreeModel model = ParticipantSelector.createModel(participants);
			participantSelector.setModel(model);
			participantSelector.setCheckingStateForPath(new TreePath(participantSelector.getRoot()), TristateCheckBoxState.CHECKED);
			participantBusyLabel.setBusy(false);

			participantSelector.expandPath(new TreePath(participantSelector.getRoot()));
		}

	}

}
