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
package ca.phon.app.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import ca.phon.formatter.FormatterUtil;
import ca.phon.project.ParticipantHistory;
import ca.phon.project.Project;
import ca.phon.session.Participant;
import ca.phon.session.SessionPath;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTree;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeCellEditor;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeCellRenderer;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeModel;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeNode;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class ParticipantSelector extends TristateCheckBoxTree {

	private static final long serialVersionUID = -2636193549260758476L;

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(ParticipantSelector.class.getName());
	
	
	public static TristateCheckBoxTreeModel createModel(Collection<Participant> participants) {
		return createModel(participants, true);
	}
	
	public static TristateCheckBoxTreeModel createModel(Collection<Participant> participants, boolean enableCheckboxes) {
		final TristateCheckBoxTreeNode root = new TristateCheckBoxTreeNode("Participants");
		root.setEnablePartialCheck(false);
		
		for(Participant participant:participants) {
			
			final DefaultMutableTreeNode node = (enableCheckboxes ? new TristateCheckBoxTreeNode(participant) : new DefaultMutableTreeNode(participant));
			if(enableCheckboxes)
				((TristateCheckBoxTreeNode)node).setEnablePartialCheck(false);
			root.add(node);
			
			final ParticipantHistory history = participant.getExtension(ParticipantHistory.class);
			if(history != null) {
				for(SessionPath sessionPath:history.getSessions()) {
					String info = sessionPath.toString() + 
							(history.getAgeForSession(sessionPath) != null ? ", Age: " + FormatterUtil.format(history.getAgeForSession(sessionPath)) : "") +
							", # of records: " + history.getNumberOfRecordsForSession(sessionPath);
					final DefaultMutableTreeNode infoNode = new DefaultMutableTreeNode(info);
					node.add(infoNode);
				}
			}
		}
		
		return new TristateCheckBoxTreeModel(root);
		
	}
	
	public ParticipantSelector() {
		this(new TristateCheckBoxTreeModel(new TristateCheckBoxTreeNode("Participants")));
	}
	
	public ParticipantSelector(TristateCheckBoxTreeModel model) {
		super(model);
		
		init();
	}
	
	private void init() {
		ImageIcon sessionIcon = IconManager.getInstance().getIcon(
				"mimetypes/text-xml", IconSize.SMALL);
		final ImageIcon participantIcon = IconManager.getInstance().getIcon("apps/system-users", IconSize.SMALL);
		
		final TristateCheckBoxTreeCellRenderer renderer = new TristateCheckBoxTreeCellRenderer();
		renderer.setLeafIcon(sessionIcon);
		renderer.setClosedIcon(participantIcon);
		renderer.setOpenIcon(participantIcon);
		
		final TristateCheckBoxTreeCellRenderer editorRenderer = new TristateCheckBoxTreeCellRenderer();
		editorRenderer.setLeafIcon(sessionIcon);
		editorRenderer.setClosedIcon(participantIcon);
		editorRenderer.setOpenIcon(participantIcon);
		final TristateCheckBoxTreeCellEditor editor = new TristateCheckBoxTreeCellEditor(this, editorRenderer);
		
		setCellRenderer(renderer);
		setCellEditor(editor);
		
		super.expandRow(0);
	}
	
	public void loadParticipants(Project project, List<SessionPath> sessionPaths) {
		setModel(createModel(project.getParticipants(sessionPaths)));
	}
	
	public List<Participant> getSelectedParticpants() {
		final List<Participant> retVal = new ArrayList<>();
		
		for(TreePath treePath:getCheckedPaths()) {
			final Object lastPathObj = treePath.getLastPathComponent();
			if(lastPathObj instanceof TristateCheckBoxTreeNode) {
				final TristateCheckBoxTreeNode node = (TristateCheckBoxTreeNode)lastPathObj;
				if(node.getUserObject() instanceof Participant) {
					retVal.add((Participant)node.getUserObject());
				}
			}
		}
		
		return retVal;
	}
	
}
