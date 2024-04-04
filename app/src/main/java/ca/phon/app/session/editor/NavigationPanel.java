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

import ca.phon.app.session.editor.actions.*;
import ca.phon.ui.FlatButton;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconSize;
import com.jgoodies.forms.layout.*;

import javax.swing.*;
import java.awt.event.*;
import java.lang.ref.WeakReference;
import java.util.List;

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
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RecordChanged, this::onRecordChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RecordAdded, this::onRecordAdded, EditorEventManager.RunOn.AWTEventDispatchThread);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RecordDeleted, this::onRecordDeleted, EditorEventManager.RunOn.AWTEventDispatchThread);
	}
	
	private void init() {
		final Action firstRecordAction = new FirstRecordAction(getEditor());
		firstRecordAction.putValue(FlatButton.ICON_NAME_PROP, "FIRST_PAGE");
		firstRecordAction.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
		firstRecordButton = new FlatButton(firstRecordAction);
		firstRecordButton.setAction(firstRecordAction);
		firstRecordButton.setText(null);
		firstRecordButton.setFocusable(false);
		
		final Action lastRecordAction = new LastRecordAction(getEditor());
		lastRecordAction.putValue(FlatButton.ICON_NAME_PROP, "LAST_PAGE");
		lastRecordAction.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
		lastRecordButton = new FlatButton(lastRecordAction);
		lastRecordButton.setAction(lastRecordAction);
		lastRecordButton.setText(null);
		lastRecordButton.setFocusable(false);
		
		final Action prevRecordAction = new PreviousRecordAction(getEditor());
		prevRecordAction.putValue(FlatButton.ICON_NAME_PROP, "CHEVRON_LEFT");
		prevRecordAction.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
		prevRecordButton = new FlatButton(prevRecordAction);
		prevRecordButton.setAction(prevRecordAction);
		prevRecordButton.setText(null);
		prevRecordButton.setFocusable(false);
		
		final Action nextRecordAction = new NextRecordAction(getEditor());
		nextRecordAction.putValue(FlatButton.ICON_NAME_PROP, "CHEVRON_RIGHT");
		nextRecordAction.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
		nextRecordButton = new FlatButton(nextRecordAction);
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
		final PhonUIAction gotoRecordAct = PhonUIAction.runnable(this::gotoRecord);
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
		
		final JPanel buttonPanel = new JPanel(new FormLayout("pref, pref, pref, pref", "pref"));
		buttonPanel.add(firstRecordButton, cc.xy(1, 1));
		buttonPanel.add(prevRecordButton, cc.xy(2, 1));
		buttonPanel.add(nextRecordButton, cc.xy(3, 1));
		buttonPanel.add(lastRecordButton, cc.xy(4, 1));
		
		JLabel rl = new JLabel(" Record: ");
		add(rl, cc.xy(1, 1));
		add(recordNumberField, cc.xy(2,1));
		JLabel ol = new JLabel(" of ");
		add(ol, cc.xy(3, 1));
		add(numRecordsLabel, cc.xy(4,1));
		add(buttonPanel, cc.xy(6, 1));
	}
	
	public void gotoRecord() {
		final Integer recordNumber = Integer.parseInt(recordNumberField.getText());
		getEditor().setCurrentRecordIndex(recordNumber-1);
	}
	
	/** Editor events */
	private void onRecordChanged(EditorEvent<EditorEventType.RecordChangedData> ee) {
		if(!recordNumberField.hasFocus())
			recordNumberField.setText(""+(getEditor().getCurrentRecordIndex()+1));
	}

	private void onRecordAdded(EditorEvent<EditorEventType.RecordAddedData> ee) {
		numRecordsLabel.setText(""+getEditor().getDataModel().getRecordCount());
		recordNumberField.setMaxNumber(getEditor().getDataModel().getRecordCount());
	}

	public void onRecordDeleted(EditorEvent ee) {
		numRecordsLabel.setText(""+getEditor().getDataModel().getRecordCount());
		recordNumberField.setMaxNumber(getEditor().getDataModel().getRecordCount());
	}
	
}
