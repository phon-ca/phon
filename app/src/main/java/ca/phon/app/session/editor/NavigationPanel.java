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
package ca.phon.app.session.editor;

import java.awt.event.*;
import java.lang.ref.*;
import java.util.*;

import javax.swing.*;

import com.jgoodies.forms.layout.*;

import ca.phon.app.session.editor.actions.*;
import ca.phon.ui.action.*;

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
	}
	
	private void init() {
		final ButtonGroup btnGroup = new ButtonGroup();
		final List<JButton> buttons = (new SegmentedButtonBuilder<JButton>(JButton::new)).createSegmentedButtons(4, btnGroup);
		
		final Action firstRecordAction = new FirstRecordAction(getEditor());
		firstRecordButton = buttons.get(0);
		firstRecordButton.setAction(firstRecordAction);
		firstRecordButton.setText(null);
		firstRecordButton.setFocusable(false);
		
		final Action lastRecordAction = new LastRecordAction(getEditor());
		lastRecordButton = buttons.get(3);
		lastRecordButton.setAction(lastRecordAction);
		lastRecordButton.setText(null);
		lastRecordButton.setFocusable(false);
		
		final Action prevRecordAction = new PreviousRecordAction(getEditor());
		prevRecordButton = buttons.get(1);
		prevRecordButton.setAction(prevRecordAction);
		prevRecordButton.setText(null);
		prevRecordButton.setFocusable(false);
		
		final Action nextRecordAction = new NextRecordAction(getEditor());
		nextRecordButton = buttons.get(2);
		nextRecordButton.setAction(nextRecordAction);
		nextRecordButton.setText(null);
		nextRecordButton.setFocusable(false);
		
		recordNumberField = new RecordNumberField();
		recordNumberField.setMinNumber(1);
		recordNumberField.setMaxNumber(getEditor().getDataModel().getRecordCount());
		recordNumberField.setColumns(3);
		recordNumberField.setEditable(true);
		if(getEditor().getCurrentRecordIndex() >= 0)
			recordNumberField.setText(Integer.toString(getEditor().getCurrentRecordIndex()+1));
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
		
		final JComponent btnComp = (new SegmentedButtonBuilder<JButton>(JButton::new)).createLayoutComponent(buttons);
		
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
		if(!recordNumberField.hasFocus())
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
	
}
