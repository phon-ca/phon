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
package ca.phon.app.project;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXBusyLabel;

import ca.phon.app.session.*;
import ca.phon.project.*;
import ca.phon.session.*;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.tristatecheckbox.*;

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

		this.project = project;
		if(project != null) {
			this.cache = project.getExtension(ParticipantCache.class);
		}

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

	public void setProject(Project project) {
		this.project = project;
		this.cache = project.getExtension(ParticipantCache.class);
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

			final Set<Participant> participants = cache.getParticipants(processedPaths);
			final TristateCheckBoxTreeModel model = ParticipantSelector.createModel(participants);
			participantSelector.setModel(model);
			participantSelector.setCheckingStateForPath(new TreePath(participantSelector.getRoot()), TristateCheckBoxState.CHECKED);
			participantBusyLabel.setBusy(false);

			participantSelector.expandPath(new TreePath(participantSelector.getRoot()));
		}

	}

}
