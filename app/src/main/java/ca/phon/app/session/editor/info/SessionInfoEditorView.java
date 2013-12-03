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
package ca.phon.app.session.editor.info;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.AbstractAction;
import javax.swing.Action;
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
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXTable;
import org.joda.time.DateTime;

import ca.phon.app.project.ProjectFrameExtension;
import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.DividedEditorView;
import ca.phon.app.session.editor.DockPosition;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.AddParticipantEdit;
import ca.phon.app.session.editor.undo.RemoveParticipantEdit;
import ca.phon.app.session.editor.undo.SessionDateEdit;
import ca.phon.app.session.participant.ParticipantEditor;
import ca.phon.app.session.participant.ParticipantsTableModel;
import ca.phon.media.util.MediaLocator;
import ca.phon.project.Project;
import ca.phon.session.DateFormatter;
import ca.phon.session.Participant;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.DateTimeDocument;
import ca.phon.ui.FileSelectionField;
import ca.phon.ui.PromptedTextField.FieldState;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.PathExpander;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 */
public class SessionInfoEditorView extends DividedEditorView {

	private final String VIEW_TITLE = "Session Information";

	/**
	 * Date editor
	 */
	private JTextField dateField;
	
	/**
	 * Media field
	 */
	private MediaSelectionField mediaLocationField;
	
	/**
	 * Language
	 */
	private JTextField languageField;

	private final DocumentListener languageFieldListener = new DocumentListener() {
		
		@Override
		public void changedUpdate(DocumentEvent e) {
			updateLang();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			updateLang();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updateLang();
		}
		
		private void updateLang() {
//			String oldVal = getModel().getSession().getLanguage();
//			String newVal = languageField.getText();
//			if(!oldVal.equals(newVal)) {
//				getModel().getSession().setLanguage(languageField.getText());
//				getModel().fireRecordEditorEvent(SESSION_LANG_CHANGED, SessionInfoEditorView.this, languageField.getText());
//			}
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
		
//		ParticipantsTableModel tableModel = new ParticipantsTableModel(
//				IPhonFactory.getDefaultFactory().createTranscript());
		participantTable = new JXTable();
		participantTable.setVisibleRowCount(3);
		
		ComponentInputMap participantTableInputMap = new ComponentInputMap(participantTable);
		ActionMap participantTableActionMap = new ActionMap();
		
//		final KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		Action deleteAction = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				deleteParticipant();
			}
			
		};
		participantTableActionMap.put("DELETE_PARTICIPANT", deleteAction);
		participantTableInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_PARTICIPANT");
		participantTableInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "DELETE_PARTICIPANT");
		
		participantTable.setInputMap(WHEN_FOCUSED, participantTableInputMap);
		participantTable.setActionMap(participantTableActionMap);
		
		ImageIcon addIcon = 
			IconManager.getInstance().getIcon("actions/add_user", IconSize.XSMALL);
		addParticipantButton = new JButton(addIcon);
		addParticipantButton.setFocusable(false);
		addParticipantButton.setToolTipText("Add participant");
		addParticipantButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				newParticipant();
			}
			
		});
		
		ImageIcon editIcon = 
			IconManager.getInstance().getIcon("actions/edit_user", IconSize.XSMALL);
		editParticipantButton = new JButton(editIcon);
		editParticipantButton.setFocusable(false);
		editParticipantButton.setToolTipText("Edit participant");
		editParticipantButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				editParticipant();
			}
			
		});
		
		FormLayout participantLayout = new FormLayout(
				"fill:pref:grow, pref",
				"pref, pref, pref:grow");
		JPanel participantPanel = new JPanel(participantLayout);
		participantPanel.setBackground(Color.white);
		participantPanel.add(new JScrollPane(participantTable), cc.xywh(1, 1, 1, 3));
		participantPanel.add(addParticipantButton, cc.xy(2,1));
		participantPanel.add(editParticipantButton, cc.xy(2,2));
		
		languageField = new JTextField();
//		languageField.setText(model.getSession().getLanguage());
//		languageField.setBorder(null);
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

	public JTextField createDateField() {
		final JTextField retVal = new JTextField();
		
		final DateTime sessionDate = DateTime.now();
		
		retVal.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				updateSessionDate();
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
			}
		});
		
		final DateTimeDocument doc = new DateTimeDocument(sessionDate);
		retVal.setDocument(doc);
		
		return retVal;
	}
	
	@Override
	public String getName() {
		return VIEW_TITLE;
	}

	private void update() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getDataModel().getSession();
		
		final ProjectFrameExtension pfe = editor.getExtension(ProjectFrameExtension.class);
		Project project = null;
		if(pfe != null) {
			project = pfe.getProject();
		}
		
		final DateTimeDocument doc = (DateTimeDocument)dateField.getDocument();
		doc.setDateTime(session.getDate());
		
		final File mediaFile = MediaLocator.findMediaFile(project,  session);
		if(mediaFile != null) {
			mediaLocationField.removePropertyChangeListener(FileSelectionField.FILE_PROP, mediaLocationListener);
			mediaLocationField.setFile(mediaFile);
			mediaLocationField.addPropertyChangeListener(FileSelectionField.FILE_PROP, mediaLocationListener);
		} else {
			if(session.getMediaLocation() != null &&
					StringUtils.strip(session.getMediaLocation()).length() > 0) {
				mediaLocationField.setState(FieldState.INPUT);
				mediaLocationField.setText(session.getMediaLocation());
			} else {
				mediaLocationField.setState(FieldState.PROMPT);
			}
		}
		
		languageField.getDocument().removeDocumentListener(languageFieldListener);
		languageField.setText(session.getLanguage());
		languageField.getDocument().addDocumentListener(languageFieldListener);
		
		ParticipantsTableModel tableModel = new ParticipantsTableModel(session);
		participantTable.setModel(tableModel);
	}

	private void updateSessionDate() {
		final DateTimeDocument doc = (DateTimeDocument)dateField.getDocument();
		
		final SessionEditor editor = getEditor();
		final Session session = editor.getDataModel().getSession();
		
		final DateTime currentDate = session.getDate();
		final DateTime newDate = doc.getDateTime();
		
		if(!currentDate.isEqual(newDate)) {
			final SessionDateEdit edit = new SessionDateEdit(getEditor(), newDate, currentDate);
			edit.setSource(dateField);
			editor.getUndoSupport().postEdit(edit);
		}
		
	}
	
	private void newParticipant() {
		final SessionFactory factory = SessionFactory.newFactory();
		final Participant part = factory.createParticipant();
		boolean canceled = ParticipantEditor.editParticipant(getEditor(), part, getEditor().getDataModel().getSession().getDate());
		
		if(!canceled) {
			final AddParticipantEdit edit = new AddParticipantEdit(getEditor(), part);
			edit.setSource(participantTable);
			getEditor().getUndoSupport().postEdit(edit);
		}
	}
	
	private void editParticipant() {
//		int selectedRow = participantTable.getSelectedRow();
//		if(selectedRow < 0) return;
//		selectedRow = participantTable.convertRowIndexToModel(selectedRow);
//		if(selectedRow >= 0 && selectedRow < getModel().getSession().getParticipants().size()) {
//			Participant part = getModel().getSession().getParticipants().get(selectedRow);
//			
//			ParticipantEditor.editParticipant(CommonModuleFrame.getCurrentFrame(), part, getModel().getSession().getDate());
//		
//			((ParticipantsTableModel)participantTable.getModel()).fireTableDataChanged();
//			getModel().fireRecordEditorEvent(PARTICIPANT_CHANGED, this, part);
//		}
	}
	
	private void deleteParticipant() {
		int selectedRow = participantTable.getSelectedRow();
		if(selectedRow < 0) return;
		selectedRow = participantTable.convertRowIndexToModel(selectedRow);
		
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		if(selectedRow >= 0 && selectedRow < session.getParticipantCount()) {
			final Participant participant = session.getParticipant(selectedRow);
			
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setParentWindow(null);
			props.setOptions(MessageDialogProperties.yesNoOptions);
			props.setRunAsync(false);
			props.setMessage("Delete participant '" + participant.getName() + ";? This action cannot be undone.");
			props.setHeader("Delete participant");
			props.setTitle("Delete participant");
			
			final int ret = NativeDialogs.showMessageDialog(props);
			if(ret == 0) {
				final RemoveParticipantEdit edit = new RemoveParticipantEdit(editor, participant);
				edit.setSource(participantTable);
				editor.getUndoSupport().postEdit(edit);
			}
			
		}
	}
	
	/** Editor actions */
	@RunOnEDT
	public void onSessionMediaChanged(EditorEvent ee) {
//		IMediaPlaybackController playbackController =
//				getModel().getPlaybackController(false);
//		if(playbackController != null) {
//			try {
//				playbackController.loadMedia(ee.getEventData().toString());
//			} catch (PhonMediaException e) {
//				PhonLogger.warning(e.toString());
//			}
//		}
	}
	
	@RunOnEDT
	public void onParticipantListChanged(EditorEvent ee) {
		((ParticipantsTableModel)participantTable.getModel()).fireTableDataChanged();
	}
	
	@RunOnEDT
	public void onDateChanged(EditorEvent ee) {
		if(ee.getSource() != dateField) {
			final DateTime newDate = (DateTime)ee.getEventData();
			dateField.setText(DateFormatter.dateTimeToString(newDate));
		}
		((ParticipantsTableModel)participantTable.getModel()).fireTableDataChanged();
	}
	
	private final PropertyChangeListener mediaLocationListener = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
//			final File mediaFile = mediaLocationField.getSelectedFile();
//			final String mediaLoc = (mediaFile == null ? null : mediaFile.getPath());
//			final PathExpander pe = new PathExpander();
//			getModel().getSession().setMediaLocation(pe.compressPath(mediaLoc));
//			getModel().fireRecordEditorEvent(SESSION_MEDIA_CHANGED, SessionInfoEditorView.this, mediaLoc);
		}
	};

	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getIcon("apps/system-users", IconSize.SMALL);
	}

	@Override
	public JMenu getMenu() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DockPosition getPreferredDockPosition() {
		return DockPosition.CENTER;
	}
	
	
}
