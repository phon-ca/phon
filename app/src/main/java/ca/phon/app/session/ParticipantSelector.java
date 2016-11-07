package ca.phon.app.session;

import java.io.IOException;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import ca.phon.project.Project;
import ca.phon.session.AgeFormatter;
import ca.phon.session.Participant;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTree;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeCellEditor;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeCellRenderer;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeModel;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeNode;
import ca.phon.util.Tuple;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class ParticipantSelector extends TristateCheckBoxTree {

	private static final long serialVersionUID = -2636193549260758476L;

	private final static Logger LOGGER = Logger.getLogger(ParticipantSelector.class.getName());
	
	/*
	 * Default participant comparator.
	 * Considers participants equal if their ID, role and name are the same
	 */
	public static class DefaultParticipantComparator implements Comparator<Participant> {

		@Override
		public int compare(Participant o1, Participant o2) {
			
			int cmp = o1.getRole().compareTo(o2.getRole());
			if(cmp == 0) {
				cmp = o1.getId().compareTo(o2.getId());
				
				if(cmp == 0) {
					cmp = o1.getName().compareTo(o2.getName());
				}
			}
			
			return cmp;
		}
		
	}
	
	private static class SessionData extends Tuple<SessionPath, Period> {
	
		public SessionData(SessionPath path, Period p) {
			super(path, p);
		}
		
		public SessionPath getSessionPath() {
			return getObj1();
		}
		
		public Period getAge() {
			return getObj2();
		}
		
		@Override
		public String toString() {
			return "Session:" + getSessionPath() + " Age:" + AgeFormatter.ageToString(getAge());
		}
		
	}
	
	public static TristateCheckBoxTreeModel createModel(Project project, List<SessionPath> sessionPaths, 
			Comparator<Participant> participantComparator) {
		final TristateCheckBoxTreeNode root = new TristateCheckBoxTreeNode("Participants");
		root.setEnablePartialCheck(false);
		
		if(project != null && sessionPaths != null) {
			final Map<Participant, List<SessionData>> participantData = new TreeMap<>(participantComparator);
			
			for(SessionPath sessionPath:sessionPaths) {
				try {
					final Session session = project.openSession(sessionPath.getCorpus(), sessionPath.getSession());
					for(Participant participant:session.getParticipants()) {
						List<SessionData> sessionData = participantData.get(participant);
						if(sessionData == null) {
							sessionData = new ArrayList<>();
							participantData.put(participant, sessionData);
						}
						sessionData.add(new SessionData(sessionPath, participant.getAge(session.getDate())));
					}
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
			
			for(Participant participant:participantData.keySet()) {
				final TristateCheckBoxTreeNode node = new TristateCheckBoxTreeNode(participant);
				node.setEnablePartialCheck(false);
				
				for(SessionData sessionData:participantData.get(participant)) {
					final DefaultMutableTreeNode dataNode = new DefaultMutableTreeNode(sessionData);
					node.add(dataNode);
				}
				root.add(node);
			}
		}
		
		return new TristateCheckBoxTreeModel(root);
	}
	
	public ParticipantSelector() {
		this(null, null);
	}
	
	public ParticipantSelector(Project project, List<SessionPath> sessionPaths) {
		this(createModel(project, sessionPaths, new DefaultParticipantComparator()));
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
		setModel(createModel(project, sessionPaths, new DefaultParticipantComparator()));
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
