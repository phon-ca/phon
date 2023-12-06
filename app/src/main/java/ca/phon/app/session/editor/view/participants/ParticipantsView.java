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
package ca.phon.app.session.editor.view.participants;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.actions.*;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.participants.actions.*;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.*;
import ca.phon.ui.participant.ParticipantsTableModel;
import ca.phon.util.icons.*;
import com.jgoodies.forms.layout.*;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.CompoundEdit;
import java.awt.*;
import java.awt.event.*;

/**
 * 
 */
public class ParticipantsView extends EditorView {

	public static final String VIEW_NAME = "Participants";

	public static final String VIEW_ICON = IconManager.FontAwesomeFontName + ":USERS";

	/**
	 * Participant Table
	 * 
	 */
	private JXTable participantTable;
	private JButton editParticipantButton;
	private JButton addParticipantButton;
	private JButton removeParticipantButton;
	
	/**
	 * Constructor
	 */
	public ParticipantsView(SessionEditor editor) {
		super(editor);
		
		init();
		setupEditorActions();
	}

	private void setupEditorActions() {
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SessionChanged, this::onSessionChanged, EditorEventManager.RunOn.AWTEventDispatchThread);

		getEditor().getEventManager().registerActionForEvent(EditorEventType.ParticipantAdded, this::onParticipantListChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.ParticipantRemoved, this::onParticipantListChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.ParticipantChanged, this::onParticipantListChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		participantTable = new JXTable();
		participantTable.setVisibleRowCount(3);
		
		ComponentInputMap participantTableInputMap = new ComponentInputMap(participantTable);
		ActionMap participantTableActionMap = new ActionMap();
		
		ImageIcon deleteIcon = 
				IconManager.getInstance().getIcon("actions/delete_user", IconSize.SMALL);
		final PhonUIAction<Void> deleteAction = PhonUIAction.runnable(this::deleteParticipant);
		deleteAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Delete selected participant");
		deleteAction.putValue(PhonUIAction.SMALL_ICON, deleteIcon);
		participantTableActionMap.put("DELETE_PARTICIPANT", deleteAction);
		participantTableInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_PARTICIPANT");
		participantTableInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "DELETE_PARTICIPANT");
		
		removeParticipantButton = new JButton(deleteAction);
		
		participantTable.setInputMap(WHEN_FOCUSED, participantTableInputMap);
		participantTable.setActionMap(participantTableActionMap);
		
		addParticipantButton = new JButton(new NewParticipantAction(getEditor()));
		addParticipantButton.setFocusable(false);
		
		ImageIcon editIcon = 
			IconManager.getInstance().getIcon("actions/edit_user", IconSize.SMALL);
		final PhonUIAction<Void> editParticipantAct = PhonUIAction.runnable(this::editParticipant);
		editParticipantAct.putValue(PhonUIAction.NAME, "Edit participant...");
		editParticipantAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Edit selected participant...");
		editParticipantAct.putValue(PhonUIAction.SMALL_ICON, editIcon);
		editParticipantButton = new JButton(editParticipantAct);
		editParticipantButton.setFocusable(false);
		
		final CellConstraints cc = new CellConstraints();
		FormLayout participantLayout = new FormLayout(
				"fill:pref:grow, pref, pref, pref",
				"pref, pref, pref:grow");
		JPanel participantPanel = new JPanel(participantLayout);
		participantPanel.setBackground(Color.white);
		participantPanel.add(new JScrollPane(participantTable), cc.xywh(1, 2, 3, 2));
		participantPanel.add(addParticipantButton, cc.xy(2,1));
		participantPanel.add(editParticipantButton, cc.xy(3,1));
		participantPanel.add(removeParticipantButton, cc.xy(4, 2));
		participantTable.addMouseListener(new MouseInputAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(arg0.getClickCount() == 2 && arg0.getButton() == MouseEvent.BUTTON1) {
					editParticipantAct.actionPerformed(
							new ActionEvent(arg0.getSource(), arg0.getID(), "edit"));
				}
			}
			
			@Override
			public void mousePressed(MouseEvent me) {
				if(me.isPopupTrigger()) {
					showParticipantContextMenu(me.getPoint());
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent me) {
				if(me.isPopupTrigger()) {
					showParticipantContextMenu(me.getPoint());
				}
			}
			
		});
		
		add(participantPanel, BorderLayout.CENTER);
		update();
	}
	
	@Override
	public String getName() {
		return VIEW_NAME;
	}

	private void update() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getDataModel().getSession();
		ParticipantsTableModel tableModel = new ParticipantsTableModel(session);
		participantTable.setModel(tableModel);
	}

	private void showParticipantContextMenu(Point p) {
		final JPopupMenu popupMenu = new JPopupMenu();
		final MenuBuilder builder = new MenuBuilder(popupMenu);
		
		final int viewIndex = participantTable.rowAtPoint(p);
		if(viewIndex >= 0 && viewIndex < participantTable.getRowCount()) {
			final int modelIndex = participantTable.convertRowIndexToModel(viewIndex);
			final Participant participant = getEditor().getSession().getParticipant(modelIndex);
			
			builder.addItem(".", new EditParticipantAction(getEditor(), participant));
			builder.addItem(".", new DeleteParticipantAction(getEditor(), participant));
			
			builder.addItem(".", new AssignUnidentifiedSpeakerAction(getEditor(), this, participant));
			
			builder.addSeparator(".", "participant_actions");
		}
		
		builder.addItem(".", new NewParticipantAction(getEditor()));
		
		popupMenu.show(participantTable, p.x, p.y);
	}
	
	public void editParticipant() {
		int selectedRow = participantTable.getSelectedRow();
		if(selectedRow < 0) return;
		selectedRow = participantTable.convertRowIndexToModel(selectedRow);
		if(selectedRow >= 0 && selectedRow < getEditor().getSession().getParticipantCount()) {
			final Participant part = getEditor().getSession().getParticipant(selectedRow);
			
			final EditParticipantAction act = new EditParticipantAction(getEditor(), part);
			act.actionPerformed(new ActionEvent(this, 0, null));
		}
	}
	
	public void assignSpeakerToUnassignedRecords(Participant p) {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		final CompoundEdit cmpEdit = new CompoundEdit();
		
		for(Record r:session.getRecords()) {
			if(r.getSpeaker() == Participant.UNKNOWN) {
				final ChangeSpeakerEdit edit = new ChangeSpeakerEdit(editor, r, p);
				edit.doIt();
				cmpEdit.addEdit(edit);
			}
		}
		
		cmpEdit.end();
		editor.getUndoSupport().postEdit(cmpEdit);
	}
	
	public void deleteParticipant() {
		int selectedRow = participantTable.getSelectedRow();
		if(selectedRow < 0) return;
		selectedRow = participantTable.convertRowIndexToModel(selectedRow);
		
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		if(selectedRow >= 0 && selectedRow < session.getParticipantCount()) {
			final Participant participant = session.getParticipant(selectedRow);
			
			final DeleteParticipantAction act = new DeleteParticipantAction(getEditor(), participant);
			act.actionPerformed(new ActionEvent(this, 0, null));
		}
	}

	/** Editor actions */
	private void onSessionChanged(EditorEvent<Session> ee) {
		update();
	}

	private void onParticipantListChanged(EditorEvent<Participant> ee) {
		((ParticipantsTableModel)participantTable.getModel()).fireTableDataChanged();
	}
	
	@Override
	public ImageIcon getIcon() {
		final String[] iconData = VIEW_ICON.split(":");
		return IconManager.getInstance().buildFontIcon(iconData[0], iconData[1], IconSize.MEDIUM, Color.darkGray);
	}

	@Override
	public JMenu getMenu() {
		final JMenu menu = new JMenu();
		final Session session = getEditor().getSession();
		for(Participant p:session.getParticipants()) {
			final JMenu speakerMenu = new JMenu(p.getName());
			
			speakerMenu.add(new EditParticipantAction(getEditor(), p));
			speakerMenu.add(new AssignUnidentifiedSpeakerAction(getEditor(), this, p));
			speakerMenu.add(new DeleteParticipantAction(getEditor(), p));
			
			menu.add(speakerMenu);
		}
		menu.add(new NewParticipantAction(getEditor()));
		
		return menu;
	}

	@Override
	public DockPosition getPreferredDockPosition() {
		return DockPosition.CENTER;
	}
	
}
