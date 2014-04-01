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
package ca.phon.app.session.editor;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.ref.WeakReference;
import java.util.List;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.phon.app.session.editor.actions.FirstRecordAction;
import ca.phon.app.session.editor.actions.LastRecordAction;
import ca.phon.app.session.editor.actions.NextRecordAction;
import ca.phon.app.session.editor.actions.PreviousRecordAction;
import ca.phon.query.replace.SessionLocation;
import ca.phon.ui.SegmentedButtonBuilder;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class NavigationPanel extends JPanel {
	
	private static final long serialVersionUID = -6569641249723608229L;

	/** 'Button's */
	private JButton firstRecordButton;
	
	private JButton prevRecordButton;
	
	private JButton nextRecordButton;
	
	private JButton lastRecordButton;
	
	private RecordNumberField recordNumberField;
	
	private JLabel numRecordsLabel;

	private JLabel currentTierLabel;

	private JLabel currentCharPosLabel;
	
	/** Editor */
	private WeakReference<SessionEditor> editorRef;
	
	private SessionEditor getEditor() {
		return editorRef.get();
	}
	
	/** Constructor */
	public NavigationPanel(SessionEditor editor) {
		super();
		
		this.editorRef = new WeakReference<SessionEditor>(editor);
		init();
		setupEditorActions();
	}

	private void setupEditorActions() {
		DelegateEditorAction recordChangedAct =
				new DelegateEditorAction(this, "onRecordChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT, recordChangedAct);

		DelegateEditorAction recordAddedAct =
				new DelegateEditorAction(this, "onRecordAdded");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_ADDED_EVT, recordAddedAct);

		DelegateEditorAction recordDeletedAct =
				new DelegateEditorAction(this, "onRecordDeleted");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_DELETED_EVT, recordDeletedAct);

		DelegateEditorAction sessionLocationChangedAct =
				new DelegateEditorAction(this, "onSessionLocationChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SESSION_LOCATION_CHANGED_EVT, sessionLocationChangedAct);
	}
	
	private void init() {
		final ButtonGroup btnGroup = new ButtonGroup();
		final List<JButton> buttons = SegmentedButtonBuilder.createSegmentedButtons(4, btnGroup);
		
		final Action firstRecordAction = new FirstRecordAction(getEditor());
		firstRecordButton = buttons.get(0);
		firstRecordButton.setAction(firstRecordAction);
		firstRecordButton.setText(null);
		
		final Action lastRecordAction = new LastRecordAction(getEditor());
		lastRecordButton = buttons.get(3);
		lastRecordButton.setAction(lastRecordAction);
		lastRecordButton.setText(null);
		
		final Action prevRecordAction = new PreviousRecordAction(getEditor());
		prevRecordButton = buttons.get(1);
		prevRecordButton.setAction(prevRecordAction);
		prevRecordButton.setText(null);
		
		final Action nextRecordAction = new NextRecordAction(getEditor());
		nextRecordButton = buttons.get(2);
		nextRecordButton.setAction(nextRecordAction);
		nextRecordButton.setText(null);
		
		recordNumberField = new RecordNumberField();
		recordNumberField.setMinNumber(1);
		recordNumberField.setMaxNumber(getEditor().getDataModel().getRecordCount());
		recordNumberField.setColumns(3);
		recordNumberField.setEditable(true);
		recordNumberField.setText("1");
		recordNumberField.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent arg0) {
				
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				recordNumberField.selectAll();
			}
		});
		
		recordNumberField.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				final String txt = recordNumberField.getText();
				if(txt.length() > 0) {
					final Integer v = Integer.parseInt(txt);
					if(getEditor().getCurrentRecordIndex() != v-1)
						getEditor().setCurrentRecordIndex(v-1);
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		final PhonUIAction gotoRecordAct = new PhonUIAction(this, "gotoRecord");
		recordNumberField.setAction(gotoRecordAct);
		
		numRecordsLabel = new JLabel("");
		numRecordsLabel.setText("" + getEditor().getDataModel().getRecordCount() + " ");
		numRecordsLabel.setOpaque(false);

		currentTierLabel = new JLabel("");
		currentTierLabel.setOpaque(false);

		currentCharPosLabel = new JLabel("");
		currentCharPosLabel.setOpaque(false);
		
		FormLayout layout = new FormLayout(
				"pref, pref, pref, pref, 5dlu, pref",
				"pref");
		setLayout(layout);
		
		CellConstraints cc = new CellConstraints();
		
		final JComponent btnComp = SegmentedButtonBuilder.createLayoutComponent(buttons);
		
		JLabel rl = new JLabel(" Record: ");
		add(rl, cc.xy(1, 1));
		add(recordNumberField, cc.xy(2,1));
		JLabel ol = new JLabel(" of ");
		add(ol, cc.xy(3, 1));
		add(numRecordsLabel, cc.xy(4,1));
		add(btnComp, cc.xy(6, 1));
	}
	
	public void gotoRecord() {
		final Integer recordNumber = Integer.parseInt(recordNumberField.getText());
		getEditor().setCurrentRecordIndex(recordNumber-1);
	}
	
	/** Editor events */
	public void onRecordChanged(EditorEvent ee) {
		recordNumberField.setText(""+(getEditor().getCurrentRecordIndex()+1));
	}

	public void onRecordAdded(EditorEvent ee) {
		numRecordsLabel.setText(""+getEditor().getDataModel().getRecordCount());
		recordNumberField.setMaxNumber(getEditor().getDataModel().getRecordCount());
	}

	public void onRecordDeleted(EditorEvent ee) {
		numRecordsLabel.setText(""+getEditor().getDataModel().getRecordCount());
		recordNumberField.setMaxNumber(getEditor().getDataModel().getRecordCount());
	}

	public void onSessionLocationChanged(EditorEvent ee) {
		SessionLocation location = (SessionLocation)ee.getEventData();
		currentTierLabel.setText(location.getRecordLocation().getTier() + "  ");
		currentCharPosLabel.setText(location.getRecordLocation().getLocation()+"");
	}
	
}
