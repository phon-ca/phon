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
package ca.phon.app.session.editor.view.session_information;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.time.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;

import org.apache.commons.lang3.*;
import org.jdesktop.swingx.*;

import com.jgoodies.forms.layout.*;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.actions.*;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.common.*;
import ca.phon.app.session.editor.view.session_information.actions.*;
import ca.phon.project.*;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.ui.action.*;
import ca.phon.ui.menu.*;
import ca.phon.ui.participant.*;
import ca.phon.ui.text.*;
import ca.phon.ui.text.PromptedTextField.*;
import ca.phon.util.icons.*;

/**
 * 
 */
public class SessionInfoEditorView extends EditorView {

	private static final long serialVersionUID = -3112381708875592956L;
	
	public static final String VIEW_TITLE = "Session Information";

	/**
	 * Date editor
	 */
	private DatePicker dateField;

	/**
	 * Media field
	 */
	private MediaSelectionField mediaLocationField;
	
	/**
	 * Language
	 */
	private LanguageField languageField;
	
	private TierDataLayoutPanel contentPanel;

	private boolean updatingLanguage = false;
	private final DocumentListener languageFieldListener = new DocumentListener() {
		
		@Override
		public void changedUpdate(DocumentEvent e) {
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			if(!updatingLanguage)
				updateLang();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			if(!updatingLanguage)
				updateLang();
		}
		
		private void updateLang() {
			final String newVal = languageField.getText();
			
			final SessionLanguageEdit edit = new SessionLanguageEdit(getEditor(), newVal);
			edit.setSource(this);
			getEditor().getUndoSupport().postEdit(edit);
		}
		
	};
	
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
	public SessionInfoEditorView(SessionEditor editor) {
		super(editor);
		
		init();
		setupEditorActions();
	}

	private void setupEditorActions() {
		final DelegateEditorAction sessionMediaChangedAct = new
				DelegateEditorAction(this, "onSessionMediaChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SESSION_MEDIA_CHANGED, sessionMediaChangedAct);
		
		final DelegateEditorAction participantsChangedAct = 
				new DelegateEditorAction(this, "onParticipantListChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.PARTICIPANT_ADDED, participantsChangedAct);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.PARTICIPANT_REMOVED, participantsChangedAct);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.PARTICIPANT_CHANGED, participantsChangedAct);
	
		final DelegateEditorAction dateChangedAct = 
				new DelegateEditorAction(this, "onDateChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SESSION_DATE_CHANGED, dateChangedAct);
		
		final DelegateEditorAction langChangedAct = 
				new DelegateEditorAction(this, "onLangChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SESSION_LANG_CHANGED, langChangedAct);
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		contentPanel = new TierDataLayoutPanel();
		
		dateField = createDateField();
		dateField.getTextField().setColumns(10);
		dateField.setBackground(Color.white);
		
		mediaLocationField = new MediaSelectionField(getEditor().getProject());
		mediaLocationField.setEditor(getEditor());
		mediaLocationField.getTextField().setColumns(10);
		mediaLocationField.addPropertyChangeListener(FileSelectionField.FILE_PROP, mediaLocationListener);
		mediaLocationField.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
			}
			
		});
		
		participantTable = new JXTable();
		participantTable.setVisibleRowCount(3);
		
		ComponentInputMap participantTableInputMap = new ComponentInputMap(participantTable);
		ActionMap participantTableActionMap = new ActionMap();
		
		ImageIcon deleteIcon = 
				IconManager.getInstance().getIcon("actions/delete_user", IconSize.SMALL);
		final PhonUIAction deleteAction = new PhonUIAction(this, "deleteParticipant");
		deleteAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Delete selected participant");
		deleteAction.putValue(PhonUIAction.SMALL_ICON, deleteIcon);
		participantTableActionMap.put("DELETE_PARTICIPANT", deleteAction);
		participantTableInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_PARTICIPANT");
		participantTableInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "DELETE_PARTICIPANT");
		
		removeParticipantButton = new JButton(deleteAction);
		
		participantTable.setInputMap(WHEN_FOCUSED, participantTableInputMap);
		participantTable.setActionMap(participantTableActionMap);
		
		addParticipantButton = new JButton(new NewParticipantAction(getEditor(), this));
		addParticipantButton.setFocusable(false);
		
		ImageIcon editIcon = 
			IconManager.getInstance().getIcon("actions/edit_user", IconSize.SMALL);
		final PhonUIAction editParticipantAct = new PhonUIAction(this, "editParticipant");
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
		
		languageField = new LanguageField();
		languageField.getDocument().addDocumentListener(languageFieldListener);
		
		int rowIdx = 0;
		final JLabel dateLbl = new JLabel("Session Date");
		dateLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		contentPanel.add(dateLbl, 
				new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, rowIdx));
		contentPanel.add(dateField, 
				new TierDataConstraint(TierDataConstraint.FLAT_TIER_COLUMN, rowIdx++));
		
		final JLabel mediaLbl = new JLabel("Media");
		mediaLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		contentPanel.add(mediaLbl,
				new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, rowIdx));
		contentPanel.add(mediaLocationField, 
				new TierDataConstraint(TierDataConstraint.FLAT_TIER_COLUMN, rowIdx++));
		
		final JLabel partLbl = new JLabel("Participants");
		partLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		contentPanel.add(partLbl,
				new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, rowIdx));
		contentPanel.add(participantPanel, 
				new TierDataConstraint(TierDataConstraint.FLAT_TIER_COLUMN, rowIdx++));
		
		final JLabel langLbl = new JLabel("Language");
		langLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		contentPanel.add(langLbl,
				new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, rowIdx));
		contentPanel.add(languageField, 
				new TierDataConstraint(TierDataConstraint.FLAT_TIER_COLUMN, rowIdx++));
		
		add(new JScrollPane(contentPanel), BorderLayout.CENTER);
		
		update();
	}
	
	public DatePicker createDateField() {
		final DatePicker retVal = new DatePicker();
		
		final LocalDate sessionDate = getEditor().getSession().getDate();
		if(sessionDate != null)
			retVal.setDateTime(sessionDate);
		
		retVal.getTextField().getDocument().addDocumentListener(new DocumentListener() {

			void dateFieldUpdate() {
				final LocalDate selectedDate = retVal.getDateTime();
				if(selectedDate == null) return;
				
				final LocalDate newDate = LocalDate.from(selectedDate);
				
				final SessionDateEdit edit = new SessionDateEdit(getEditor(), newDate, getEditor().getSession().getDate());
				edit.setSource(dateField);
				getEditor().getUndoSupport().postEdit(edit);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				if(!dateField.isValueAdjusing())
					dateFieldUpdate();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				if(!dateField.isValueAdjusing())
					dateFieldUpdate();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				
			}
			
		});

		retVal.getTextField().addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {

			}

			@Override
			public void focusLost(FocusEvent e) {
				if(retVal.getTextField().getState() == FieldState.PROMPT) {
					retVal.setDateTime(sessionDate);
				}
			}
		});

		return retVal;
	}
	
	@Override
	public String getName() {
		return VIEW_TITLE;
	}

	private void update() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getDataModel().getSession();
		
		final Project project = editor.getExtension(Project.class);
		if(project == null) return;
		
		final LocalDate sessionDate = getEditor().getSession().getDate();
		if(sessionDate != null) {
			dateField.setValueIsAdjusting(true);
			dateField.setDateTime(sessionDate);
			dateField.setValueIsAdjusting(false);
		}
		
		if(session.getMediaLocation() != null &&
				StringUtils.strip(session.getMediaLocation()).length() > 0) {
			mediaLocationField.getTextField().setState(FieldState.INPUT);
			mediaLocationField.removePropertyChangeListener(FileSelectionField.FILE_PROP, mediaLocationListener);
			mediaLocationField.setFile(new File(session.getMediaLocation()));
			mediaLocationField.addPropertyChangeListener(FileSelectionField.FILE_PROP, mediaLocationListener);
		} else {
			mediaLocationField.getTextField().setState(FieldState.PROMPT);
		}
		
		languageField.getDocument().removeDocumentListener(languageFieldListener);
		languageField.setText(session.getLanguage());
		languageField.getDocument().addDocumentListener(languageFieldListener);
		
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
			
			builder.addItem(".", new EditParticipantAction(getEditor(), this, participant));
			builder.addItem(".", new DeleteParticipantAction(getEditor(), this, participant));
			
			builder.addItem(".", new AssignUnidentifiedSpeakerAction(getEditor(), this, participant));
			
			builder.addSeparator(".", "participant_actions");
		}
		
		builder.addItem(".", new NewParticipantAction(getEditor(), this));
		
		popupMenu.show(participantTable, p.x, p.y);
	}
	
	public void editParticipant() {
		int selectedRow = participantTable.getSelectedRow();
		if(selectedRow < 0) return;
		selectedRow = participantTable.convertRowIndexToModel(selectedRow);
		if(selectedRow >= 0 && selectedRow < getEditor().getSession().getParticipantCount()) {
			final Participant part = getEditor().getSession().getParticipant(selectedRow);
			
			final EditParticipantAction act = new EditParticipantAction(getEditor(), this, part);
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
			
			final DeleteParticipantAction act = new DeleteParticipantAction(getEditor(), this, participant);
			act.actionPerformed(new ActionEvent(this, 0, null));
		}
	}
	
	/** Editor actions */
	boolean updatingMediaLocation = false;
	@RunOnEDT
	public void onSessionMediaChanged(EditorEvent ee) {
		if(ee.getSource() != this) {
			final String mediaPath = getEditor().getSession().getMediaLocation();
			updatingMediaLocation = true;
			mediaLocationField.setFile(mediaPath != null ? new File(mediaPath) : null);
			updatingMediaLocation = false;
		}
	}
	
	@RunOnEDT
	public void onParticipantListChanged(EditorEvent ee) {
		((ParticipantsTableModel)participantTable.getModel()).fireTableDataChanged();
		
		// setup menu for editor so that new participant actions for
		// segmentation are available
		getEditor().setJMenuBar(MenuManager.createWindowMenuBar(getEditor()));
	}
	
	@RunOnEDT
	public void onLangChanged(EditorEvent ee) {
		if(ee.getSource() != languageFieldListener) {
			final String newVal = (String)ee.getEventData();
			updatingLanguage = true;
			languageField.setText(newVal);
			updatingLanguage = false;
		}
	}
	
	@RunOnEDT
	public void onDateChanged(EditorEvent ee) {
		if(ee.getSource() != dateField) {
			final LocalDate newDate = (LocalDate)ee.getEventData();
			dateField.setDateTime(newDate);
		}
		((ParticipantsTableModel)participantTable.getModel()).fireTableDataChanged();
	}
	
	private final PropertyChangeListener mediaLocationListener = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if(updatingMediaLocation) return;
//			final File mediaFile = mediaLocationField.getSelectedFile();
//			final String mediaLoc = (mediaFile == null ? null : mediaFile.getPath());
			
			final MediaLocationEdit edit = new MediaLocationEdit(getEditor(), mediaLocationField.getText());
			edit.setSource(this);
			getEditor().getUndoSupport().postEdit(edit);
		}
		
	};

	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getIcon("apps/system-users", IconSize.SMALL);
	}

	@Override
	public JMenu getMenu() {
		final JMenu menu = new JMenu();
		
		menu.add(new AssignMediaAction(getEditor()));
		menu.add(new UnassignMediaAction(getEditor())).setEnabled(getEditor().getMediaModel().isSessionMediaAvailable());
		menu.addSeparator();
		
		final Session session = getEditor().getSession();
		for(Participant p:session.getParticipants()) {
			final JMenu speakerMenu = new JMenu(p.getName());
			
			speakerMenu.add(new EditParticipantAction(getEditor(), this, p));
			speakerMenu.add(new AssignUnidentifiedSpeakerAction(getEditor(), this, p));
			speakerMenu.add(new DeleteParticipantAction(getEditor(), this, p));
			
			menu.add(speakerMenu);
		}
		menu.add(new NewParticipantAction(getEditor(), this));
		
		return menu;
	}

	@Override
	public DockPosition getPreferredDockPosition() {
		return DockPosition.CENTER;
	}
	
}
