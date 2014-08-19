/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.app.session.editor.view.session_information;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXTable;
import org.joda.time.DateTime;

import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.DividedEditorView;
import ca.phon.app.session.editor.DockPosition;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.MediaLocationEdit;
import ca.phon.app.session.editor.undo.SessionDateEdit;
import ca.phon.app.session.editor.undo.SessionLanguageEdit;
import ca.phon.app.session.editor.view.session_information.actions.BrowseForMediaAction;
import ca.phon.app.session.editor.view.session_information.actions.DeleteParticipantAction;
import ca.phon.app.session.editor.view.session_information.actions.EditParticipantAction;
import ca.phon.app.session.editor.view.session_information.actions.NewParticipantAction;
import ca.phon.media.util.MediaLocator;
import ca.phon.project.Project;
import ca.phon.session.DateFormatter;
import ca.phon.session.Participant;
import ca.phon.session.Session;
import ca.phon.ui.DateTimeDocument;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.participant.ParticipantsTableModel;
import ca.phon.ui.text.DatePicker;
import ca.phon.ui.text.FileSelectionField;
import ca.phon.ui.text.PromptedTextField.FieldState;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 */
public class SessionInfoEditorView extends DividedEditorView {

	private static final long serialVersionUID = -3112381708875592956L;

	private final String VIEW_TITLE = "Session Information";

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
	private JTextField languageField;

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
	
		final DelegateEditorAction dateChangedAct = 
				new DelegateEditorAction(this, "onDateChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SESSION_DATE_CHANGED, dateChangedAct);
		
		final DelegateEditorAction langChangedAct = 
				new DelegateEditorAction(this, "onLangChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SESSION_LANG_CHANGED, langChangedAct);
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		final JPanel contentPanel = new JPanel();
		contentPanel.setBackground(Color.white);
		
		FormLayout layout = new FormLayout(
				"right:150px, 5px, fill:pref:grow",
				"pref, 3dlu, pref, 3dlu, pref, 3dlu, pref");
		CellConstraints cc = new CellConstraints();
		contentPanel.setLayout(layout);
		
		dateField = createDateField();
		dateField.setBackground(Color.white);
		
		mediaLocationField = new MediaSelectionField();
		mediaLocationField.addPropertyChangeListener(FileSelectionField.FILE_PROP, mediaLocationListener);
		
		participantTable = new JXTable();
		participantTable.setVisibleRowCount(3);
		
		ComponentInputMap participantTableInputMap = new ComponentInputMap(participantTable);
		ActionMap participantTableActionMap = new ActionMap();
		
		final PhonUIAction deleteAction = new PhonUIAction(this, "deleteParticipant");
		participantTableActionMap.put("DELETE_PARTICIPANT", deleteAction);
		participantTableInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_PARTICIPANT");
		participantTableInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "DELETE_PARTICIPANT");
		
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
		
		FormLayout participantLayout = new FormLayout(
				"fill:pref:grow, pref",
				"pref, pref, pref:grow");
		JPanel participantPanel = new JPanel(participantLayout);
		participantPanel.setBackground(Color.white);
		participantPanel.add(new JScrollPane(participantTable), cc.xywh(1, 1, 1, 3));
		participantPanel.add(addParticipantButton, cc.xy(2,1));
		participantPanel.add(editParticipantButton, cc.xy(2,2));
		
		languageField = new JTextField();
		languageField.getDocument().addDocumentListener(languageFieldListener);
		
		contentPanel.add(new JLabel("Session Date"), cc.xy(1,1));
		contentPanel.add(dateField, cc.xy(3,1));
		
		contentPanel.add(new JLabel("Media"), cc.xy(1, 3));
		contentPanel.add(mediaLocationField, cc.xy(3, 3));
		
		contentPanel.add(new JLabel("Participants"), cc.xy(1, 5));
		contentPanel.add(participantPanel, cc.xy(3, 5));
		
		contentPanel.add(new JLabel("Language"), cc.xy(1, 7));
		contentPanel.add(languageField, cc.xy(3, 7));
		
		add(new JScrollPane(contentPanel), BorderLayout.CENTER);
		
		update();
	}

	public DatePicker createDateField() {
		final DatePicker retVal = new DatePicker();
		
		retVal.setDate(getEditor().getSession().getDate().toDate());
		
		retVal.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				final Date selectedDate = retVal.getDate();
				final DateTime newDate = new DateTime(selectedDate);
				
				final SessionDateEdit edit = new SessionDateEdit(getEditor(), newDate, getEditor().getSession().getDate());
				getEditor().getUndoSupport().postEdit(edit);
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
		
		dateField.setDate(getEditor().getSession().getDate().toDate());
		
		final File mediaFile = MediaLocator.findMediaFile(project,  session);
		if(mediaFile != null) {
			mediaLocationField.removePropertyChangeListener(FileSelectionField.FILE_PROP, mediaLocationListener);
			mediaLocationField.setFile(mediaFile);
			mediaLocationField.addPropertyChangeListener(FileSelectionField.FILE_PROP, mediaLocationListener);
		} else {
			if(session.getMediaLocation() != null &&
					StringUtils.strip(session.getMediaLocation()).length() > 0) {
				mediaLocationField.getTextField().setState(FieldState.INPUT);
				mediaLocationField.setText(session.getMediaLocation());
			} else {
				mediaLocationField.getTextField().setState(FieldState.PROMPT);
			}
		}
		
		languageField.getDocument().removeDocumentListener(languageFieldListener);
		languageField.setText(session.getLanguage());
		languageField.getDocument().addDocumentListener(languageFieldListener);
		
		ParticipantsTableModel tableModel = new ParticipantsTableModel(session);
		participantTable.setModel(tableModel);
	}

	private void updateSessionDate() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getDataModel().getSession();
		
		final DateTime currentDate = session.getDate();
		final DateTime newDate = new DateTime(dateField.getDate());
		
		if(!currentDate.isEqual(newDate)) {
			final SessionDateEdit edit = new SessionDateEdit(getEditor(), newDate, currentDate);
			edit.setSource(dateField);
			editor.getUndoSupport().postEdit(edit);
		}
		
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
			mediaLocationField.setFile(new File(mediaPath));
			updatingMediaLocation = false;
		}
	}
	
	@RunOnEDT
	public void onParticipantListChanged(EditorEvent ee) {
		((ParticipantsTableModel)participantTable.getModel()).fireTableDataChanged();
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
			final DateTime newDate = (DateTime)ee.getEventData();
			dateField.setDate(newDate.toDate());
		}
		((ParticipantsTableModel)participantTable.getModel()).fireTableDataChanged();
	}
	
	private final PropertyChangeListener mediaLocationListener = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if(updatingMediaLocation) return;
			final File mediaFile = mediaLocationField.getSelectedFile();
			final String mediaLoc = (mediaFile == null ? null : mediaFile.getPath());
			
			final MediaLocationEdit edit = new MediaLocationEdit(getEditor(), mediaLoc);
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
		
		menu.add(new BrowseForMediaAction(getEditor(), this));
		menu.addSeparator();
		
		final Session session = getEditor().getSession();
		for(Participant p:session.getParticipants()) {
			final JMenu speakerMenu = new JMenu(p.getName());
			
			speakerMenu.add(new EditParticipantAction(getEditor(), this, p));
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
