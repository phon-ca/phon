/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.prefs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.ui.HidablePanel;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.PrefHelper;

/**
 * Edit general application prefs such as
 * automatic update.
 * 
 */
public class GeneralPrefsPanel extends PrefsPanel {

	private static final long serialVersionUID = 8761130200130930937L;

	private final String checkForUpdateAtStartupProp = "ca.phon.application.updater.checkOnStartup";
	
	public GeneralPrefsPanel() {
		super("General");
		
		init();
	}

	private void init() {
		CellConstraints cc = new CellConstraints();
		
		// update checking
		boolean doCheckUpdate = PrefHelper.getBoolean(checkForUpdateAtStartupProp, true);
		
		JCheckBox checkForUpdatesBox = new JCheckBox("Check for updates when application starts");
		checkForUpdatesBox.setSelected(doCheckUpdate);
		checkForUpdatesBox.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JCheckBox box = (JCheckBox)e.getSource();
				boolean doCheckUpdate = box.isSelected();
				PrefHelper.getUserPreferences().putBoolean(checkForUpdateAtStartupProp, doCheckUpdate);
			}
			
		});
		
		
		JPanel updatesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		FormLayout layout = 
			new FormLayout("fill:pref:grow", "pref, pref");
		JPanel innerPanel = new JPanel(layout);
		updatesPanel.add(checkForUpdatesBox);
		updatesPanel.setBorder(BorderFactory.createTitledBorder("Program Updates"));
		innerPanel.add(updatesPanel, cc.xy(1,1));
		
		// info messages
		PhonUIAction resetInfoMessagesAct = new PhonUIAction(this, "onResetInfoMessages");
		resetInfoMessagesAct.putValue(Action.NAME, "Reset Information Messages");
		JButton resetInfoMessagesBtn = new JButton(resetInfoMessagesAct);
		
		JPanel resetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		resetPanel.add(resetInfoMessagesBtn);
		resetPanel.setBorder(BorderFactory.createTitledBorder("Information Messages"));
		innerPanel.add(resetPanel, cc.xy(1, 2));
		
		// UI theme
		
		
		JScrollPane innerScroller = new JScrollPane(innerPanel);
		setLayout(new BorderLayout());
		add(innerScroller, BorderLayout.CENTER);
	}
	
	/**
	 * Reset hide-able messages.
	 * @param pae
	 */
	public void onResetInfoMessages(PhonActionEvent pae) {
		HidablePanel.clearSavePanelProps();
	}
	
	/**
	 * Set application UI theme.
	 * @param themeClassName
	 */
	public void onSetTheme(String themeClassName) {
		
	}
}
